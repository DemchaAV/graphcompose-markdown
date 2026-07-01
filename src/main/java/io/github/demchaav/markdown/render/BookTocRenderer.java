package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.style.DocumentLeader;
import com.demcha.compose.document.style.DocumentTextDecoration;
import com.demcha.compose.document.style.DocumentTextStyle;
import io.github.demchaav.markdown.model.TocNode;
import io.github.demchaav.markdown.theme.style.InlineStyle;

import java.util.List;

/**
 * A book-style alternative to {@link TocRenderer}: renders a {@code [TOC]} marker as a native,
 * page-numbered table of contents — each heading becomes a row whose clickable label jumps to
 * the heading, a dotted leader fills the gap, and the <em>page number is resolved automatically
 * from the laid-out document</em> (the engine performs the second layout pass; no manual
 * bookkeeping). Opt in by swapping the renderer for the {@code TocNode} type:
 *
 * <pre>{@code
 * MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
 *         .renderer(TocNode.class, new BookTocRenderer())
 *         .build();
 * }</pre>
 *
 * <p>The default {@link TocRenderer} (a plain clickable link list, no page numbers) stays the
 * screen-oriented default. Heading nesting is shown by indenting the label with spaces —
 * the engine's contents rows are flat. Empty-text headings are skipped; a document with no
 * headings renders nothing.</p>
 */
public final class BookTocRenderer implements NodeRenderer<TocNode> {

    /** Label indent per heading level below the top level. Non-breaking spaces — the same
     * idiom the code-block renderer uses — because plain leading spaces are collapsed by the
     * text layout and the base-14 fonts carry no em-space glyph. */
    private static final String INDENT = "   ";

    private final String title;

    /** Creates a book TOC with no title row (write your own heading in the Markdown). */
    public BookTocRenderer() {
        this(null);
    }

    /**
     * Creates a book TOC with a title row above the entries.
     *
     * @param title the contents title (e.g. {@code "Contents"}), or {@code null} for none
     */
    public BookTocRenderer(String title) {
        this.title = title == null || title.isBlank() ? null : title.strip();
    }

    @Override
    public void render(TocNode node, SectionBuilder host, RenderContext ctx) {
        List<TocEntry> entries = ctx.tocEntries().stream()
                .filter(entry -> !entry.text().isEmpty())
                .toList();
        if (entries.isEmpty()) {
            return;
        }
        int minLevel = entries.stream().mapToInt(TocEntry::level).min().orElse(1);
        InlineStyle base = ctx.paragraphInline();
        var typo = ctx.tokens().typography();
        DocumentTextStyle entryStyle = DocumentTextStyle.builder()
                .fontName(base.family().resolve(false, false))
                .size(base.size())
                .color(base.color())
                .decoration(DocumentTextDecoration.DEFAULT)
                .build();
        DocumentTextStyle pageStyle = DocumentTextStyle.builder()
                .fontName(base.family().resolve(false, false))
                .size(base.size())
                .color(ctx.tokens().colors().muted())
                .decoration(DocumentTextDecoration.DEFAULT)
                .build();
        DocumentTextStyle titleStyle = DocumentTextStyle.builder()
                .fontName(typo.headingFamily().resolve(true, false))
                .size(typo.headingSize(2))
                .color(ctx.tokens().colors().heading())
                .decoration(DocumentTextDecoration.DEFAULT)
                .build();

        host.addTableOfContents(toc -> {
            if (title != null) {
                toc.title(title).titleStyle(titleStyle);
            }
            toc.leader(DocumentLeader.DOTS)
                    .leaderColor(ctx.tokens().colors().muted())
                    .entryStyle(entryStyle)
                    .pageNumberStyle(pageStyle);
            for (TocEntry entry : entries) {
                toc.entry(INDENT.repeat(entry.level() - minLevel) + entry.text(), entry.slug());
            }
        });
    }
}
