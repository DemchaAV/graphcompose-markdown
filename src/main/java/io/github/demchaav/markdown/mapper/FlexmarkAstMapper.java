package io.github.demchaav.markdown.mapper;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import io.github.demchaav.markdown.model.*;
import io.github.demchaav.markdown.model.inline.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static String stripTrailingNewline(String code) {
        if (code.endsWith("\r\n")) {
            return code.substring(0, code.length() - 2);
        }
        if (code.endsWith("\n") || code.endsWith("\r")) {
            return code.substring(0, code.length() - 1);
        }
        return code;
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
        return new MarkdownDocument(mapBlocks(document));
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
            return new CodeBlockNode(firstToken(fenced.getInfo()), stripTrailingNewline(fenced.getContentChars().toString()));
        }
        if (node instanceof IndentedCodeBlock indented) {
            return new CodeBlockNode("", stripTrailingNewline(indented.getContentChars().toString()));
        }
        if (node instanceof BlockQuote quote) {
            return new QuoteNode(mapBlocks(quote));
        }
        if (node instanceof ThematicBreak) {
            return new ThematicBreakNode();
        }
        if (node instanceof TableBlock table) {
            return mapTable(table);
        }
        // Unsupported block (raw HTML block, etc.) — skipped for now.
        return null;
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
                items.add(new ListItemNode(mapBlocks(item)));
            }
        }
        return new ListNode(ordered, startNumber, items);
    }

    private List<InlineNode> mapInlines(Node parent) {
        List<InlineNode> result = new ArrayList<>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
            InlineNode mapped = mapInline(child);
            if (mapped != null) {
                result.add(mapped);
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
        if (node instanceof HtmlEntity entity) {
            return new TextRun(decodeEntity(entity.getChars().toString()));
        }
        if (node instanceof HtmlInline inline) {
            String html = inline.getChars().toString().trim().toLowerCase();
            if (html.equals("<br>") || html.equals("<br/>") || html.equals("<br />")) {
                return new LineBreakRun(true);
            }
            return null; // other inline HTML is dropped for now
        }
        return null;
    }
}
