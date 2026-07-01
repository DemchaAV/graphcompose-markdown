package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.style.DocumentInsets;
import com.demcha.compose.document.style.DocumentTextDecoration;
import com.demcha.compose.document.style.DocumentTextStyle;
import io.github.demchaav.markdown.model.TocNode;
import io.github.demchaav.markdown.theme.style.InlineStyle;

import java.util.List;

/**
 * Renders a {@code [TOC]} marker as an auto-generated, clickable table of contents: one
 * internal link per heading, indented by heading level, each jumping to the heading's anchor.
 * Heading slugs are planned up front, so a {@code [TOC]} above its headings still resolves
 * (forward references). Empty-text headings are skipped; a document with no headings renders
 * nothing.
 */
public final class TocRenderer implements NodeRenderer<TocNode> {

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
        double bodySize = ctx.tokens().typography().bodySize();
        double indentPer = ctx.styles().list().indent();
        double lineSpacing = ctx.styles().lineLeading(bodySize);
        DocumentTextStyle linkStyle = DocumentTextStyle.builder()
                .fontName(base.family().resolve(false, false))
                .size(base.size())
                .color(base.linkColor())
                .decoration(base.underlineLinks()
                        ? DocumentTextDecoration.UNDERLINE
                        : DocumentTextDecoration.DEFAULT)
                .build();
        host.addSection(toc -> {
            toc.spacing(ctx.styles().list().itemSpacing());
            for (TocEntry entry : entries) {
                double indent = indentPer * (entry.level() - minLevel);
                RichText rich = RichText.empty();
                rich.linkTo(entry.text(), linkStyle, entry.slug());
                toc.addParagraph(p -> p.rich(rich)
                        .margin(new DocumentInsets(0, 0, 0, indent))
                        .lineSpacing(lineSpacing));
            }
        });
    }
}
