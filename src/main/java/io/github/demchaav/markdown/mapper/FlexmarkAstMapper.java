package io.github.demchaav.markdown.mapper;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.emoji.Emoji;
import com.vladsch.flexmark.ext.footnotes.Footnote;
import com.vladsch.flexmark.ext.footnotes.FootnoteBlock;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import io.github.demchaav.markdown.model.*;
import io.github.demchaav.markdown.model.inline.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps a Flexmark AST into the library's independent {@link MarkdownDocument}
 * model.
 *
 * <p>This is the boundary that keeps the rest of the library free of Flexmark
 * types: everything downstream operates on the semantic model only. Unsupported
 * block types (raw HTML blocks, tables before the table extension lands) are
 * skipped rather than rendered verbatim.</p>
 */
public final class FlexmarkAstMapper {

    private static final Map<String, String> NAMED_ENTITIES = Map.ofEntries(
            Map.entry("amp", "&"), Map.entry("lt", "<"), Map.entry("gt", ">"),
            Map.entry("quot", "\""), Map.entry("apos", "'"), Map.entry("nbsp", " "),
            Map.entry("copy", "©"), Map.entry("reg", "®"), Map.entry("trade", "™"),
            Map.entry("mdash", "—"), Map.entry("ndash", "–"), Map.entry("hellip", "…"));

    // Footnote label -> number, assigned by first-reference order, scoped to the current
    // mapping call. Flexmark does not populate footnote ordinals at parse time, and the
    // CustomBlockParser may map the document in several segments, so we number footnotes
    // ourselves over the whole document and resolve references by label (not node identity).
    private final ThreadLocal<Map<String, Integer>> footnoteNumbers =
            ThreadLocal.withInitial(Map::of);

    private static TableRow firstRow(Node section) {
        for (Node child = section.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof TableRow row) {
                return row;
            }
        }
        return null;
    }

    private static ColumnAlignment alignmentOf(TableCell cell) {
        TableCell.Alignment alignment = cell.getAlignment();
        if (alignment == null) {
            return ColumnAlignment.NONE;
        }
        switch (alignment) {
            case LEFT:
                return ColumnAlignment.LEFT;
            case CENTER:
                return ColumnAlignment.CENTER;
            case RIGHT:
                return ColumnAlignment.RIGHT;
            default:
                return ColumnAlignment.NONE;
        }
    }

    private static int clampLevel(int level) {
        return Math.max(1, Math.min(6, level));
    }

    private static String firstToken(BasedSequence info) {
        if (info == null || info.isNull()) {
            return "";
        }
        String value = info.unescape().trim();
        int space = value.indexOf(' ');
        return space < 0 ? value : value.substring(0, space);
    }

    private static String titleOrNull(BasedSequence title) {
        if (title == null || title.isNull() || title.isBlank()) {
            return null;
        }
        return title.unescape();
    }

    private static final Pattern ALERT_MARKER =
            Pattern.compile("^\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]\\s*$", Pattern.CASE_INSENSITIVE);

    /** The alert type if the blockquote's first line is a GitHub alert marker, else {@code null}. */
    private static AlertType alertType(BlockQuote quote) {
        Node first = quote.getFirstChild();
        if (!(first instanceof Paragraph)) {
            return null;
        }
        // The marker must be alone on the first line (GitHub requires it).
        String firstLine = first.getChars().toString().split("\n", 2)[0].strip();
        Matcher matcher = ALERT_MARKER.matcher(firstLine);
        return matcher.matches() ? AlertType.valueOf(matcher.group(1).toUpperCase(Locale.ROOT)) : null;
    }

    /** Drops the alert marker line from the first paragraph of the already-mapped content. */
    private static List<MarkdownNode> stripAlertMarker(List<MarkdownNode> content) {
        if (content.isEmpty() || !(content.get(0) instanceof ParagraphNode first)) {
            return content;
        }
        List<InlineNode> inlines = first.content();
        int breakAt = -1;
        for (int i = 0; i < inlines.size(); i++) {
            if (inlines.get(i) instanceof LineBreakRun) {
                breakAt = i;
                break;
            }
        }
        List<MarkdownNode> body = new ArrayList<>(content);
        if (breakAt < 0) {
            body.remove(0); // the first paragraph was only the marker line
        } else {
            body.set(0, new ParagraphNode(new ArrayList<>(inlines.subList(breakAt + 1, inlines.size()))));
        }
        return body;
    }

    private static String normalizeCodeText(String code) {
        // Normalise CRLF/CR (Windows-authored Markdown keeps interior \r in the fenced
        // content) to LF so the renderer's per-line split never leaves a stray carriage
        // return, then drop the single trailing newline the parser includes.
        String normalized = code.replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.endsWith("\n")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String decodeEntity(String entity) {
        if (!entity.startsWith("&") || !entity.endsWith(";") || entity.length() < 3) {
            return entity;
        }
        String body = entity.substring(1, entity.length() - 1);
        try {
            if (body.startsWith("#x") || body.startsWith("#X")) {
                return new String(Character.toChars(Integer.parseInt(body.substring(2), 16)));
            }
            if (body.startsWith("#")) {
                return new String(Character.toChars(Integer.parseInt(body.substring(1))));
            }
        } catch (IllegalArgumentException ignored) {
            return entity;
        }
        return NAMED_ENTITIES.getOrDefault(body, entity);
    }

    /**
     * Maps a Flexmark document node into a {@link MarkdownDocument}.
     *
     * @param document the Flexmark document (root) node
     * @return the mapped semantic document
     */
    public MarkdownDocument map(com.vladsch.flexmark.util.ast.Document document) {
        Map<String, Integer> numbers = numberFootnotes(document);
        List<MarkdownNode> blocks = new ArrayList<>(mapSegment(document, numbers));
        List<FootnoteDefinitionNode> footnotes = footnoteDefinitions(document, numbers);
        if (!footnotes.isEmpty()) {
            blocks.add(new FootnotesNode(footnotes));
        }
        return new MarkdownDocument(blocks);
    }

    /**
     * Maps the blocks of one parsed segment, resolving footnote references against the
     * given label→number map. Does not collect footnote definitions (the caller appends a
     * single document-wide {@link FootnotesNode}).
     *
     * @param document        the parsed (sub)document
     * @param footnoteNumbers the document-wide footnote label→number map
     * @return the mapped blocks
     */
    public List<MarkdownNode> mapSegment(com.vladsch.flexmark.util.ast.Document document,
                                         Map<String, Integer> footnoteNumbers) {
        this.footnoteNumbers.set(footnoteNumbers);
        try {
            return mapBlocks(document);
        } finally {
            this.footnoteNumbers.remove();
        }
    }

    /**
     * Numbers every referenced footnote in a document by the order its first reference
     * appears. Computed over the whole document so segmented mapping stays consistent.
     *
     * @param document the full parsed document
     * @return a footnote label→number map
     */
    public Map<String, Integer> numberFootnotes(com.vladsch.flexmark.util.ast.Document document) {
        Map<String, Integer> numbers = new LinkedHashMap<>();
        assignFootnoteNumbers(document, numbers, new int[]{1});
        return numbers;
    }

    /**
     * Collects the footnote definitions of a document, numbered via the given map.
     *
     * @param document the full parsed document
     * @param numbers  the footnote label→number map
     * @return the definitions, ordered by number
     */
    public List<FootnoteDefinitionNode> footnoteDefinitions(com.vladsch.flexmark.util.ast.Document document,
                                                            Map<String, Integer> numbers) {
        this.footnoteNumbers.set(numbers);
        try {
            List<FootnoteDefinitionNode> definitions = new ArrayList<>();
            for (Node child = document.getFirstChild(); child != null; child = child.getNext()) {
                if (child instanceof FootnoteBlock block) {
                    Integer number = numbers.get(footnoteLabel(block.getText()));
                    if (number != null) {
                        definitions.add(new FootnoteDefinitionNode(number, mapBlocks(block)));
                    }
                }
            }
            definitions.sort(Comparator.comparingInt(FootnoteDefinitionNode::number));
            return definitions;
        } finally {
            this.footnoteNumbers.remove();
        }
    }

    private void assignFootnoteNumbers(Node node, Map<String, Integer> numbers, int[] next) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Footnote footnote) {
                String label = footnoteLabel(footnote.getText());
                if (!label.isEmpty() && !numbers.containsKey(label)) {
                    numbers.put(label, next[0]++);
                }
            }
            assignFootnoteNumbers(child, numbers, next);
        }
    }

    private static String footnoteLabel(BasedSequence text) {
        return text == null ? "" : text.toString().trim();
    }

    private List<MarkdownNode> mapBlocks(Node parent) {
        List<MarkdownNode> result = new ArrayList<>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
            MarkdownNode mapped = mapBlock(child);
            if (mapped != null) {
                result.add(mapped);
            }
        }
        return result;
    }

    private MarkdownNode mapBlock(Node node) {
        if (node instanceof Heading heading) {
            return new HeadingNode(clampLevel(heading.getLevel()), mapInlines(heading));
        }
        if (node instanceof Paragraph paragraph) {
            return mapParagraph(paragraph);
        }
        if (node instanceof BulletList bulletList) {
            return mapList(bulletList, false, 1);
        }
        if (node instanceof OrderedList orderedList) {
            return mapList(orderedList, true, orderedList.getStartNumber());
        }
        if (node instanceof FencedCodeBlock fenced) {
            return new CodeBlockNode(firstToken(fenced.getInfo()), normalizeCodeText(fenced.getContentChars().toString()));
        }
        if (node instanceof IndentedCodeBlock indented) {
            return new CodeBlockNode("", normalizeCodeText(indented.getContentChars().toString()));
        }
        if (node instanceof BlockQuote quote) {
            AlertType alert = alertType(quote);
            List<MarkdownNode> content = mapBlocks(quote);
            return alert != null ? new AlertNode(alert, stripAlertMarker(content)) : new QuoteNode(content);
        }
        if (node instanceof ThematicBreak) {
            return new ThematicBreakNode();
        }
        if (node instanceof TableBlock table) {
            return mapTable(table);
        }
        if (node instanceof YamlFrontMatterBlock) {
            AbstractYamlFrontMatterVisitor visitor = new AbstractYamlFrontMatterVisitor();
            visitor.visit(node);
            return new FrontMatterNode(visitor.getData());
        }
        if (node instanceof FootnoteBlock) {
            return null; // definitions are collected and rendered as a Notes section at the end
        }
        // Unmodelled block (raw HTML block, HTML comment, …): preserve the source text so
        // it is surfaced rather than silently lost (strict mode rejects it; see the composer).
        String raw = node.getChars().toString();
        return raw.isBlank() ? null : new UnsupportedBlockNode(node.getNodeName(), raw);
    }

    private MarkdownNode mapTable(TableBlock table) {
        List<ColumnAlignment> alignments = new ArrayList<>();
        List<TableCellNode> header = new ArrayList<>();
        List<List<TableCellNode>> bodyRows = new ArrayList<>();

        for (Node section = table.getFirstChild(); section != null; section = section.getNext()) {
            if (section instanceof TableHead head) {
                TableRow headerRow = firstRow(head);
                if (headerRow != null) {
                    for (Node cell = headerRow.getFirstChild(); cell != null; cell = cell.getNext()) {
                        if (cell instanceof TableCell tableCell) {
                            header.add(new TableCellNode(mapInlines(tableCell)));
                            alignments.add(alignmentOf(tableCell));
                        }
                    }
                }
            } else if (section instanceof TableBody body) {
                for (Node row = body.getFirstChild(); row != null; row = row.getNext()) {
                    if (row instanceof TableRow tableRow) {
                        bodyRows.add(mapRow(tableRow));
                    }
                }
            }
        }

        // A GFM table is only valid with a header (which defines the columns).
        return alignments.isEmpty() ? null : new TableNode(alignments, header, bodyRows);
    }

    private List<TableCellNode> mapRow(TableRow row) {
        List<TableCellNode> cells = new ArrayList<>();
        for (Node cell = row.getFirstChild(); cell != null; cell = cell.getNext()) {
            if (cell instanceof TableCell tableCell) {
                cells.add(new TableCellNode(mapInlines(tableCell)));
            }
        }
        return cells;
    }

    private MarkdownNode mapParagraph(Paragraph paragraph) {
        // CommonMark wraps a standalone image in a paragraph; promote it to a
        // block image so a theme can render it as a figure rather than text.
        Node first = paragraph.getFirstChild();
        if (first instanceof Image image && first.getNext() == null) {
            return new ImageNode(image.getUrl().toString(), image.getText().toString(), titleOrNull(image.getTitle()));
        }
        return new ParagraphNode(mapInlines(paragraph));
    }

    private ListNode mapList(Node list, boolean ordered, int startNumber) {
        List<ListItemNode> items = new ArrayList<>();
        for (Node child = list.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof ListItem item) {
                Boolean checked = item instanceof TaskListItem task ? task.isItemDoneMarker() : null;
                items.add(new ListItemNode(mapBlocks(item), checked));
            }
        }
        return new ListNode(ordered, startNumber, items);
    }

    private List<InlineNode> mapInlines(Node parent) {
        List<InlineNode> result = new ArrayList<>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof TextBase) {
                // A transparent wrapper Flexmark inserts (e.g. for autolinking) — flatten it,
                // so the Link/Text nodes inside are mapped rather than swallowed whole.
                result.addAll(mapInlines(child));
            } else {
                InlineNode mapped = mapInline(child);
                if (mapped != null) {
                    result.add(mapped);
                }
            }
        }
        return result;
    }

    private InlineNode mapInline(Node node) {
        if (node instanceof Text text) {
            return new TextRun(text.getChars().unescape());
        }
        if (node instanceof StrongEmphasis strong) {
            return new StrongRun(mapInlines(strong));
        }
        if (node instanceof Emphasis emphasis) {
            return new EmphasisRun(mapInlines(emphasis));
        }
        if (node instanceof Strikethrough strikethrough) {
            return new StrikethroughRun(mapInlines(strikethrough));
        }
        if (node instanceof Code code) {
            return new CodeRun(code.getText().toString());
        }
        if (node instanceof Link link) {
            return new LinkRun(link.getUrl().toString(), titleOrNull(link.getTitle()), mapInlines(link));
        }
        if (node instanceof AutoLink autoLink) {
            String url = autoLink.getUrl().toString();
            return new LinkRun(url, null, List.of(new TextRun(url)));
        }
        if (node instanceof MailLink mailLink) {
            String email = mailLink.getText().toString();
            return new LinkRun("mailto:" + email, null, List.of(new TextRun(email)));
        }
        if (node instanceof Image image) {
            return new ImageRun(image.getUrl().toString(), image.getText().toString(), titleOrNull(image.getTitle()));
        }
        if (node instanceof SoftLineBreak) {
            return new LineBreakRun(false);
        }
        if (node instanceof HardLineBreak) {
            return new LineBreakRun(true);
        }
        if (node instanceof Footnote footnote) {
            Integer number = footnoteNumbers.get().get(footnoteLabel(footnote.getText()));
            return number != null ? new FootnoteRefRun(number) : null;
        }
        if (node instanceof Emoji emoji) {
            return new EmojiRun(emoji.getText().toString());
        }
        if (node instanceof LinkRef || node instanceof ImageRef) {
            // An undefined reference link/image (e.g. [TODO], [text][missing]) renders as its
            // literal source in CommonMark — brackets and all. Surface it as plain text, not
            // "unsupported" content, so strict mode does not reject ordinary bracketed text.
            return new TextRun(node.getChars().toString());
        }
        if (node instanceof HtmlEntity entity) {
            return new TextRun(decodeEntity(entity.getChars().toString()));
        }
        if (node instanceof HtmlInline inline) {
            String raw = inline.getChars().toString();
            String html = raw.trim().toLowerCase();
            if (html.equals("<br>") || html.equals("<br/>") || html.equals("<br />")) {
                return new LineBreakRun(true);
            }
            return new UnsupportedInlineRun(raw); // surface other inline HTML, don't drop it
        }
        // Unmodelled inline: keep the source so it is surfaced rather than silently lost.
        String raw = node.getChars().toString();
        return raw.isBlank() ? null : new UnsupportedInlineRun(raw);
    }
}
