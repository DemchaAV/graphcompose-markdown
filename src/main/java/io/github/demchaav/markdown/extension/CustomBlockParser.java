package io.github.demchaav.markdown.extension;

import io.github.demchaav.markdown.mapper.FlexmarkAstMapper;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.FootnoteDefinitionNode;
import io.github.demchaav.markdown.model.FootnotesNode;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.parser.FlexmarkMarkdownParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts {@code :::} fenced custom blocks before/around Flexmark parsing.
 *
 * <pre>{@code
 * :::callout warning
 * This configuration is unsafe.
 * :::
 * }</pre>
 *
 * <p>Flexmark has no notion of {@code :::} fences, so this parser scans the raw
 * Markdown line-by-line: a line like {@code :::type variant} opens a block, a
 * bare {@code :::} closes it (nesting is supported via depth counting). Text
 * outside a fence is handed to the normal Flexmark parser + mapper; the content
 * inside a fence is parsed recursively, so custom blocks may contain ordinary
 * Markdown and other custom blocks.</p>
 */
public final class CustomBlockParser {

    private static final Pattern OPEN =
            Pattern.compile("^:::+\\s*([A-Za-z0-9][\\w-]*)(?:\\s+(\\S+))?.*$");
    private static final Pattern CLOSE = Pattern.compile("^:::+\\s*$");

    private final FlexmarkMarkdownParser parser;
    private final FlexmarkAstMapper mapper;

    /**
     * Creates a custom-block parser over a Flexmark parser and mapper.
     *
     * @param parser the Flexmark parser for normal segments
     * @param mapper the AST mapper for normal segments
     */
    public CustomBlockParser(FlexmarkMarkdownParser parser, FlexmarkAstMapper mapper) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    /**
     * Parses Markdown, extracting {@code :::} custom blocks.
     *
     * @param markdown the Markdown source ({@code null} treated as empty)
     * @return the semantic document
     */
    public MarkdownDocument parse(String markdown) {
        String normalized = (markdown == null ? "" : markdown).replace("\r\n", "\n").replace('\r', '\n');

        // Footnotes are document-global, but the segmented parse below would split a
        // reference from its definition. Resolve them once over the whole document: number
        // every footnote and collect its definitions from a single full parse, then map the
        // segments against that shared numbering and append one footnotes block at the end.
        com.vladsch.flexmark.util.ast.Document full = parser.parse(normalized);
        Map<String, Integer> footnoteNumbers = mapper.numberFootnotes(full);
        List<FootnoteDefinitionNode> footnotes = mapper.footnoteDefinitions(full, footnoteNumbers);

        List<MarkdownNode> blocks = parseBlocks(normalized, footnoteNumbers);
        if (!footnotes.isEmpty()) {
            blocks.add(new FootnotesNode(footnotes));
        }
        return new MarkdownDocument(blocks);
    }

    private List<MarkdownNode> parseBlocks(String text, Map<String, Integer> footnoteNumbers) {
        String[] lines = text.split("\n", -1);
        List<MarkdownNode> result = new ArrayList<>();
        StringBuilder normal = new StringBuilder();
        int i = 0;
        while (i < lines.length) {
            Matcher open = OPEN.matcher(lines[i].strip());
            if (isOpen(lines[i]) && open.matches()) {
                flushNormal(normal, result, footnoteNumbers);
                String type = open.group(1).toLowerCase();
                String variant = open.group(2);

                int depth = 1;
                int j = i + 1;
                int innerStart = j;
                while (j < lines.length) {
                    if (isOpen(lines[j])) {
                        depth++;
                    } else if (isClose(lines[j])) {
                        depth--;
                        if (depth == 0) {
                            break;
                        }
                    }
                    j++;
                }
                String inner = String.join("\n", List.of(lines).subList(innerStart, Math.min(j, lines.length)));
                result.add(new CustomBlockNode(type, variant, parseBlocks(inner, footnoteNumbers)));
                i = (j < lines.length) ? j + 1 : j; // skip the closing fence
            } else {
                normal.append(lines[i]).append('\n');
                i++;
            }
        }
        flushNormal(normal, result, footnoteNumbers);
        return result;
    }

    private void flushNormal(StringBuilder normal, List<MarkdownNode> result, Map<String, Integer> footnoteNumbers) {
        if (normal.length() == 0) {
            return;
        }
        String segment = normal.toString();
        normal.setLength(0);
        if (segment.isBlank()) {
            return;
        }
        result.addAll(mapper.mapSegment(parser.parse(segment), footnoteNumbers));
    }

    private static boolean isOpen(String line) {
        return OPEN.matcher(line.strip()).matches() && !isClose(line);
    }

    private static boolean isClose(String line) {
        return CLOSE.matcher(line.strip()).matches();
    }
}
