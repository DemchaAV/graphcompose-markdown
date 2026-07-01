package io.github.demchaav.markdown.parser;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses Markdown text into a Flexmark AST.
 *
 * <p>This is the only place in the library that touches Flexmark. The configured
 * {@link Parser} enables CommonMark plus the GFM extensions for tables, task lists,
 * strikethrough and footnotes, and the extensions for emoji shortcodes, autolinking
 * and YAML front matter — plus, optionally, typographic smart punctuation. A single
 * parser instance is reused across calls; Flexmark's {@code parse} is safe to call
 * concurrently once the parser is built.</p>
 */
public final class FlexmarkMarkdownParser {

    private final Parser parser;

    /** Creates a parser for CommonMark plus the GFM, emoji, autolink and front-matter extensions. */
    public FlexmarkMarkdownParser() {
        this(false);
    }

    /**
     * Creates a parser, optionally with typographic smart punctuation.
     *
     * @param smartPunctuation when {@code true}, straight quotes become curly quotes,
     *                         {@code --} an en-dash, {@code ---} an em-dash and {@code ...}
     *                         an ellipsis (code spans and code blocks stay verbatim)
     */
    public FlexmarkMarkdownParser(boolean smartPunctuation) {
        MutableDataSet options = new MutableDataSet();
        List<Extension> extensions = new ArrayList<>(List.of(
                StrikethroughExtension.create(), TablesExtension.create(),
                TaskListExtension.create(), FootnoteExtension.create(),
                EmojiExtension.create(), AutolinkExtension.create(),
                YamlFrontMatterExtension.create()));
        if (smartPunctuation) {
            extensions.add(TypographicExtension.create());
        }
        options.set(Parser.EXTENSIONS, extensions);
        this.parser = Parser.builder(options).build();
    }

    /**
     * Parses Markdown into a Flexmark document node.
     *
     * @param markdown the Markdown source; {@code null} is treated as empty
     * @return the root Flexmark {@link Document}
     */
    public Document parse(String markdown) {
        return parser.parse(markdown == null ? "" : markdown);
    }
}
