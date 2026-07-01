package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentInsets;
import com.demcha.compose.document.style.DocumentTextDecoration;
import com.demcha.compose.document.style.DocumentTextStyle;
import io.github.demchaav.markdown.model.FootnoteDefinitionNode;
import io.github.demchaav.markdown.model.FootnotesNode;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;

import java.util.List;

/**
 * Renders the document-end "Notes" section: a divider, a title, and the numbered definitions.
 * Each note declares its {@code fn-N} anchor (the body citation jumps here) and its marker
 * links back to the citation's {@code fnref-N} anchor, making footnotes bidirectional.
 */
public final class FootnotesRenderer implements NodeRenderer<FootnotesNode> {

    @Override
    public void render(FootnotesNode node, SectionBuilder host, RenderContext ctx) {
        if (node.definitions().isEmpty()) {
            return;
        }
        MarkdownStyles.RuleStyle rule = ctx.styles().rule();
        double bodySize = ctx.tokens().typography().bodySize();
        // Footnote text renders at 0.92x body size, so leading is relative to that, not full body.
        double lineSpacing = ctx.styles().lineLeading(bodySize * 0.92);
        DocumentColor noteColor = ctx.tokens().colors().muted();
        DocumentColor accent = ctx.tokens().colors().link();

        InlineStyle base = ctx.styles().paragraphInline();
        InlineStyle noteInline = new InlineStyle(base.family(), bodySize * 0.92, noteColor, false, false,
                base.codeFamily(), base.codeSize() * 0.92, base.codeColor(), base.codeBackground(),
                base.linkColor(), base.underlineLinks());
        DocumentTextStyle markerStyle = DocumentTextStyle.builder()
                .fontName(base.family().resolve(false, false)).size(bodySize * 0.92).color(accent)
                .decoration(DocumentTextDecoration.DEFAULT).build();
        DocumentTextStyle titleStyle = DocumentTextStyle.builder()
                .fontName(ctx.tokens().typography().headingFamily().resolve(true, false))
                .size(bodySize * 1.05).color(ctx.tokens().colors().heading())
                .decoration(DocumentTextDecoration.DEFAULT).build();
        double gap = ctx.styles().blockSpacing();

        host.addSection(notes -> {
            notes.spacing(ctx.tokens().spacing().listItemSpacing());
            notes.margin(new DocumentInsets(gap, 0, 0, 0));
            notes.addLine(line -> line.horizontal(rule.width()).color(rule.color()).thickness(rule.thickness()));
            notes.addParagraph(p -> p.text("Notes").textStyle(titleStyle));
            for (FootnoteDefinitionNode definition : node.definitions()) {
                int number = definition.number();
                String noteAnchor = "fn-" + number;       // where the body citation jumps to
                String backAnchor = "fnref-" + number;    // where this note's marker jumps back to
                notes.addSection(item -> {
                    item.spacing(2);
                    List<MarkdownNode> content = definition.content();
                    int start = 0;
                    if (!content.isEmpty() && content.get(0) instanceof ParagraphNode first) {
                        RichText rich = RichText.empty();
                        rich.linkTo("[" + number + "]", markerStyle, backAnchor);
                        rich.style("  ", markerStyle);
                        ctx.inline().appendInto(rich, first.content(), noteInline);
                        item.addParagraph(p -> p.rich(rich).lineSpacing(lineSpacing).anchor(noteAnchor));
                        start = 1;
                    } else {
                        RichText rich = RichText.empty();
                        rich.linkTo("[" + number + "]", markerStyle, backAnchor);
                        item.addParagraph(p -> p.rich(rich).anchor(noteAnchor));
                    }
                    RenderContext childCtx = ctx.withTextColor(noteColor);
                    for (int i = start; i < content.size(); i++) {
                        childCtx.renderBlock(content.get(i), item);
                    }
                });
            }
        });
    }
}
