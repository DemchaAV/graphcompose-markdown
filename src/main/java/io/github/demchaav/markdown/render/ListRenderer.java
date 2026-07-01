package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentInsets;
import com.demcha.compose.document.style.DocumentStroke;
import com.demcha.compose.document.style.DocumentTextDecoration;
import com.demcha.compose.document.style.DocumentTextStyle;
import com.demcha.compose.document.style.ShapeOutline;
import io.github.demchaav.markdown.model.ListItemNode;
import io.github.demchaav.markdown.model.ListNode;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;

import java.util.List;

/** Renders ordered/unordered lists, including nesting, as indented marker lines. */
public final class ListRenderer implements NodeRenderer<ListNode> {

    @Override
    public void render(ListNode node, SectionBuilder host, RenderContext ctx) {
        renderList(node, host, ctx, 0);
    }

    private static void renderList(ListNode node, SectionBuilder host, RenderContext ctx, int depth) {
        MarkdownStyles.ListStyle style = ctx.styles().list();
        double indent = style.indent() * (depth + 1);
        DocumentTextStyle markerStyle = DocumentTextStyle.builder()
                .fontName(ctx.tokens().typography().bodyFamily().resolve(false, false))
                .size(ctx.tokens().typography().bodySize())
                .color(style.markerColor())
                .decoration(DocumentTextDecoration.DEFAULT)
                .build();
        // A loose list (blank lines between items) gets the wider block spacing so its
        // items read as separate paragraphs; a tight list stays compact.
        double interItemSpacing = node.loose() ? ctx.styles().blockSpacing() : style.itemSpacing();
        host.addSection(listSection -> {
            listSection.spacing(interItemSpacing);
            int number = node.startNumber();
            for (ListItemNode item : node.items()) {
                String orderedMarker = number + ".";
                renderItem(item, listSection, ctx, node.ordered(), orderedMarker, markerStyle, indent, depth);
                number++;
            }
        });
    }

    private static void renderItem(ListItemNode item, SectionBuilder listSection, RenderContext ctx,
                                   boolean ordered, String orderedMarker, DocumentTextStyle markerStyle,
                                   double indent, int depth) {
        double lineSpacing = ctx.styles().lineLeading(ctx.tokens().typography().bodySize());
        DocumentInsets itemMargin = new DocumentInsets(0, 0, 0, indent);
        listSection.addSection(itemSec -> {
            itemSec.spacing(ctx.styles().list().itemSpacing());
            List<MarkdownNode> content = item.content();
            int start = 0;
            if (!content.isEmpty() && content.get(0) instanceof ParagraphNode first) {
                InlineStyle base = ctx.paragraphInline();
                RichText rich = RichText.empty();
                prependMarker(rich, item, ordered, orderedMarker, depth, markerStyle, ctx);
                ctx.inline().appendInto(rich, first.content(), base);
                // A list item renders its leading paragraph inline (bypassing ParagraphRenderer),
                // so place the footnote back-anchor here too — otherwise a footnote first cited in
                // a list item would have a forward link but a dead back-link.
                String backAnchor = ctx.footnoteBackAnchor(first.content());
                itemSec.addParagraph(p -> {
                    p.rich(rich).margin(itemMargin).lineSpacing(lineSpacing);
                    if (backAnchor != null) {
                        p.anchor(backAnchor);
                    }
                });
                start = 1;
            } else {
                RichText rich = RichText.empty();
                prependMarker(rich, item, ordered, orderedMarker, depth, markerStyle, ctx);
                itemSec.addParagraph(p -> p.rich(rich).margin(itemMargin));
            }
            for (int i = start; i < content.size(); i++) {
                MarkdownNode child = content.get(i);
                if (child instanceof ListNode nested) {
                    renderList(nested, itemSec, ctx, depth + 1);
                } else {
                    ctx.renderBlock(child, itemSec);
                }
            }
        });
    }

    /**
     * Prepends the item marker: a real inline checkbox for task-list items, the
     * ordinal ("1.") for ordered lists, or a depth-varying vector bullet
     * (filled disc → ring → diamond) for unordered lists.
     */
    private static void prependMarker(RichText rich, ListItemNode item, boolean ordered,
                                      String orderedMarker, int depth, DocumentTextStyle markerStyle,
                                      RenderContext ctx) {
        if (item.isTask()) {
            boolean checked = Boolean.TRUE.equals(item.checked());
            double size = ctx.tokens().typography().bodySize() * 0.92;
            DocumentColor accent = ctx.tokens().colors().accent();
            DocumentColor box = checked ? accent : ctx.styles().list().markerColor();
            rich.checkbox(size, checked, ShapeOutline.CheckmarkStyle.HEAVY, box, accent);
            rich.plain("  ");
        } else if (ordered) {
            rich.style(orderedMarker + "  ", markerStyle);
        } else {
            appendBullet(rich, depth, ctx);
            rich.plain("  ");
        }
    }

    /** Appends a small vector bullet whose shape cycles with nesting depth. */
    private static void appendBullet(RichText rich, int depth, RenderContext ctx) {
        DocumentColor color = ctx.styles().list().markerColor();
        double base = ctx.tokens().typography().bodySize();
        int kind = depth % 3;
        if (kind == 1) {
            // hollow ring
            rich.dot(base * 0.36, color.withOpacity(0.0), new DocumentStroke(color, 0.9));
        } else if (kind == 2) {
            // diamond
            rich.diamond(base * 0.42, color);
        } else {
            // filled disc
            rich.dot(base * 0.30, color);
        }
    }
}
