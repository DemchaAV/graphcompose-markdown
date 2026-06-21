package io.github.demchaav.markdown.parser;

import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.List;

/**
 * Parses Markdown text into a Flexmark AST.
 *
 * <p>This is the only place in the library that touches Flexmark. The configured
 * {@link Parser} enables CommonMark plus the GFM strikethrough extension. A
 * single parser instance is reused across calls; Flexmark's {@code parse} is
 * safe to call concurrently once the parser is built.</p>
 */
public final class FlexmarkMarkdownParser {

    private final Parser parser;

    /** Creates a parser configured for CommonMark plus GFM strikethrough. */
    public FlexmarkMarkdownParser() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(
                StrikethroughExtension.create(), TablesExtension.create(),
                TaskListExtension.create(), FootnoteExtension.create(),
                EmojiExtension.create()));
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
