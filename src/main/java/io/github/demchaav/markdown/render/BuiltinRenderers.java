package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.ParagraphBuilder;
import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.image.DocumentImageFitMode;
import com.demcha.compose.document.node.TextAlign;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentCornerRadius;
import com.demcha.compose.document.style.DocumentInsets;
import com.demcha.compose.document.style.DocumentStroke;
import com.demcha.compose.document.style.DocumentTextDecoration;
import com.demcha.compose.document.style.DocumentTextStyle;
import com.demcha.compose.document.style.ShapeOutline;
import com.demcha.compose.document.table.DocumentTableCell;
import com.demcha.compose.document.table.DocumentTableColumn;
import com.demcha.compose.document.table.DocumentTableStyle;
import com.demcha.compose.document.table.DocumentTableTextAnchor;
import io.github.demchaav.markdown.model.CodeBlockNode;
import io.github.demchaav.markdown.model.ColumnAlignment;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.model.ImageNode;
import io.github.demchaav.markdown.model.ListItemNode;
import io.github.demchaav.markdown.model.ListNode;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.QuoteNode;
import io.github.demchaav.markdown.model.TableCellNode;
import io.github.demchaav.markdown.model.TableNode;
import io.github.demchaav.markdown.model.ThematicBreakNode;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.theme.RendererRegistry;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The default {@link NodeRenderer} implementations and their registration.
 *
 * <p>Each renderer adds exactly one child to the host section so the host's
 * uniform block spacing applies cleanly between blocks. Panels (code blocks,
 * callouts) and accented asides (blockquotes) are nested sections that grow with
 * their content, so they paginate correctly.</p>
 */
public final class BuiltinRenderers {

    private BuiltinRenderers() {
    }

    /**
     * Registers the default renderers for all built-in node types.
     *
     * @param registry the registry to populate
     */
    public static void registerDefaults(RendererRegistry registry) {
        registry.register(HeadingNode.class, new HeadingRenderer());
        registry.register(ParagraphNode.class, new ParagraphRenderer());
        registry.register(ListNode.class, new ListRenderer());
        registry.register(CodeBlockNode.class, new CodeBlockRenderer());
        registry.register(QuoteNode.class, new QuoteRenderer());
        registry.register(ThematicBreakNode.class, new ThematicBreakRenderer());
        registry.register(ImageNode.class, new ImageRenderer());
        registry.register(TableNode.class, new TableRenderer());
        registry.register(CustomBlockNode.class, new CalloutRenderer());
    }

    /** Renders a heading as a styled paragraph with space above it. */
    public static final class HeadingRenderer implements NodeRenderer<HeadingNode> {
        @Override
        public void render(HeadingNode node, SectionBuilder host, RenderContext ctx) {
            InlineStyle base = ctx.headingInline(node.level());
            RichText rich = ctx.toRich(node.content(), base);
            double above = ctx.styles().headingSpaceAbove(node.level());
            host.addParagraph(p -> p.rich(rich).margin(new DocumentInsets(above, 0, 0, 0)));
        }
    }

    /** Renders a paragraph of inline content. */
    public static final class ParagraphRenderer implements NodeRenderer<ParagraphNode> {
        @Override
        public void render(ParagraphNode node, SectionBuilder host, RenderContext ctx) {
            if (node.content().isEmpty()) {
                return;
            }
            InlineStyle base = ctx.paragraphInline();
            RichText rich = ctx.toRich(node.content(), base);
            double lineSpacing = ctx.tokens().typography().lineSpacing();
            host.addParagraph(p -> p.rich(rich).lineSpacing(lineSpacing));
        }
    }

    /** Renders a fenced/indented code block as a rounded, padded panel, one paragraph per line. */
    public static final class CodeBlockRenderer implements NodeRenderer<CodeBlockNode> {
        @Override
        public void render(CodeBlockNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.CodeBlockStyle style = ctx.styles().codeBlock();
            DocumentTextStyle codeStyle = DocumentTextStyle.builder()
                    .fontName(style.family().resolve(false, false))
                    .size(style.size())
                    .color(style.textColor())
                    .decoration(DocumentTextDecoration.DEFAULT)
                    .build();
            String[] lines = node.code().isEmpty() ? new String[]{" "} : node.code().split("\n", -1);
            host.addSection(panel -> {
                panel.softPanel(style.background(), style.cornerRadius(), style.padding());
                panel.keepTogether();
                panel.spacing(1);
                for (String line : lines) {
                    String content = line.isEmpty() ? " " : line;
                    panel.addParagraph(p -> p.text(content).textStyle(codeStyle).lineSpacing(style.lineSpacing()));
                }
            });
        }
    }

    /** Renders a blockquote as a left-accented aside whose children are recoloured. */
    public static final class QuoteRenderer implements NodeRenderer<QuoteNode> {
        @Override
        public void render(QuoteNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.QuoteStyle style = ctx.styles().quote();
            RenderContext childCtx = ctx.withTextColor(style.textColor());
            double padding = style.padding();
            host.addSection(quote -> {
                quote.fillColor(style.background());
                // Round only the right corners so the plate sits flush against the left accent bar.
                quote.cornerRadius(DocumentCornerRadius.right(style.cornerRadius()));
                quote.accentLeft(style.barColor(), style.barWidth());
                quote.padding(new DocumentInsets(padding, padding, padding, padding + style.barWidth()));
                quote.keepTogether();
                childCtx.renderBlocks(node.content(), quote);
            });
        }
    }

    /** Renders a thematic break as a horizontal rule. */
    public static final class ThematicBreakRenderer implements NodeRenderer<ThematicBreakNode> {
        @Override
        public void render(ThematicBreakNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.RuleStyle style = ctx.styles().rule();
            host.addLine(line -> line.horizontal(style.width()).color(style.color()).thickness(style.thickness()));
        }
    }

    /** Renders an image fitted to the content width, falling back to muted alt text. */
    public static final class ImageRenderer implements NodeRenderer<ImageNode> {
        @Override
        public void render(ImageNode node, SectionBuilder host, RenderContext ctx) {
            double width = ctx.tokens().page().contentWidth();
            Optional<byte[]> bytes = ctx.images().resolve(node.source());
            if (bytes.isPresent()) {
                byte[] data = bytes.get();
                host.addImage(img -> img.source(data).fitToBounds(width, width).fitMode(DocumentImageFitMode.CONTAIN));
            } else {
                String alt = node.alt().isEmpty() ? node.source() : node.alt();
                DocumentTextStyle muted = DocumentTextStyle.builder()
                        .fontName(ctx.tokens().typography().bodyFamily().resolve(false, true))
                        .size(ctx.tokens().typography().bodySize())
                        .color(ctx.tokens().colors().muted())
                        .decoration(DocumentTextDecoration.DEFAULT)
                        .build();
                host.addParagraph(p -> p.text("[" + alt + "]").textStyle(muted));
            }
        }
    }

    /** Renders ordered/unordered lists, including nesting, as indented marker lines. */
    public static final class ListRenderer implements NodeRenderer<ListNode> {
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
            host.addSection(listSection -> {
                listSection.spacing(style.itemSpacing());
                int number = node.startNumber();
                for (ListItemNode item : node.items()) {
                    String marker = node.ordered() ? (number + ".") : style.bulletGlyph();
                    renderItem(item, listSection, ctx, marker, markerStyle, indent, depth);
                    number++;
                }
            });
        }

        private static void renderItem(ListItemNode item, SectionBuilder listSection, RenderContext ctx,
                                       String marker, DocumentTextStyle markerStyle, double indent, int depth) {
            double lineSpacing = ctx.tokens().typography().lineSpacing();
            DocumentInsets itemMargin = new DocumentInsets(0, 0, 0, indent);
            listSection.addSection(itemSec -> {
                itemSec.spacing(ctx.styles().list().itemSpacing());
                List<MarkdownNode> content = item.content();
                int start = 0;
                if (!content.isEmpty() && content.get(0) instanceof ParagraphNode first) {
                    InlineStyle base = ctx.paragraphInline();
                    RichText rich = RichText.empty();
                    prependMarker(rich, item, marker, markerStyle, ctx);
                    ctx.inline().appendInto(rich, first.content(), base);
                    itemSec.addParagraph(p -> p.rich(rich).margin(itemMargin).lineSpacing(lineSpacing));
                    start = 1;
                } else {
                    RichText rich = RichText.empty();
                    prependMarker(rich, item, marker, markerStyle, ctx);
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
         * Prepends the item marker: a real inline checkbox for task-list items
         * (rounded frame + centred check), otherwise the styled bullet/number.
         */
        private static void prependMarker(RichText rich, ListItemNode item, String marker,
                                          DocumentTextStyle markerStyle, RenderContext ctx) {
            if (item.isTask()) {
                boolean checked = Boolean.TRUE.equals(item.checked());
                double size = ctx.tokens().typography().bodySize() * 0.92;
                DocumentColor accent = ctx.tokens().colors().accent();
                DocumentColor box = checked ? accent : ctx.styles().list().markerColor();
                rich.checkbox(size, checked, ShapeOutline.CheckmarkStyle.HEAVY, box, accent);
                rich.plain("  ");
            } else {
                rich.style(marker + "  ", markerStyle);
            }
        }
    }

    /** Renders a GFM table with even fixed columns, a styled header row, and per-column alignment. */
    public static final class TableRenderer implements NodeRenderer<TableNode> {
        @Override
        public void render(TableNode node, SectionBuilder host, RenderContext ctx) {
            int cols = node.columnCount();
            if (cols == 0) {
                return;
            }
            MarkdownStyles.TableStyle style = ctx.styles().table();
            double colWidth = (ctx.tokens().page().contentWidth() - 2.0) / cols;
            DocumentStroke border = new DocumentStroke(style.border(), style.borderWidth());
            InlineStyle base = ctx.styles().paragraphInline();
            InlineStyle headerInline = cellInline(base, style, style.headerText(), true);
            InlineStyle bodyInline = cellInline(base, style, style.bodyText(), false);

            host.addTable(table -> {
                DocumentTableColumn[] columns = new DocumentTableColumn[cols];
                for (int i = 0; i < cols; i++) {
                    columns[i] = DocumentTableColumn.fixed(colWidth);
                }
                table.columns(columns);
                table.headerCells(buildRow(node.header(), node.alignments(), cols, ctx, headerInline, style, border, true));
                for (List<TableCellNode> row : node.bodyRows()) {
                    table.rowCells(buildRow(row, node.alignments(), cols, ctx, bodyInline, style, border, false));
                }
            });
        }

        private static List<DocumentTableCell> buildRow(List<TableCellNode> cells, List<ColumnAlignment> alignments,
                                                        int cols, RenderContext ctx, InlineStyle inlineStyle,
                                                        MarkdownStyles.TableStyle style, DocumentStroke border,
                                                        boolean header) {
            List<DocumentTableCell> out = new ArrayList<>(cols);
            for (int i = 0; i < cols; i++) {
                ColumnAlignment alignment = i < alignments.size() ? alignments.get(i) : ColumnAlignment.NONE;
                List<InlineNode> content = i < cells.size() ? cells.get(i).content() : List.of();
                RichText rich = ctx.inline().render(content, inlineStyle);
                var paragraph = new ParagraphBuilder().rich(rich).align(textAlign(alignment)).build();
                DocumentTableStyle.Builder cellStyle = DocumentTableStyle.builder()
                        .padding(style.cellPadding())
                        .stroke(border)
                        .textAnchor(anchor(alignment))
                        .fillColor(header ? style.headerFill() : style.rowFill());
                out.add(DocumentTableCell.node(paragraph).withStyle(cellStyle.build()));
            }
            return out;
        }

        private static InlineStyle cellInline(InlineStyle base, MarkdownStyles.TableStyle style,
                                              DocumentColor color, boolean bold) {
            return new InlineStyle(style.family(), style.fontSize(), color, bold, false,
                    base.codeFamily(), base.codeSize(), base.codeColor(), base.linkColor(), base.underlineLinks());
        }

        private static TextAlign textAlign(ColumnAlignment alignment) {
            switch (alignment) {
                case CENTER:
                    return TextAlign.CENTER;
                case RIGHT:
                    return TextAlign.RIGHT;
                default:
                    return TextAlign.LEFT;
            }
        }

        private static DocumentTableTextAnchor anchor(ColumnAlignment alignment) {
            switch (alignment) {
                case CENTER:
                    return DocumentTableTextAnchor.CENTER;
                case RIGHT:
                    return DocumentTableTextAnchor.CENTER_RIGHT;
                default:
                    return DocumentTableTextAnchor.CENTER_LEFT;
            }
        }
    }

    /** Renders a {@code :::} custom block as a tinted, left-accented callout. */
    public static final class CalloutRenderer implements NodeRenderer<CustomBlockNode> {
        @Override
        public void render(CustomBlockNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.CalloutStyle style = ctx.styles().callout();
            DocumentColor accent = accentFor(node.variant(), style.accent());
            DocumentColor background = accent.withOpacity(0.12);
            host.addSection(panel -> {
                // Round only the right corners — the left edge meets the accent bar.
                panel.softPanel(background, DocumentCornerRadius.right(style.cornerRadius()), style.padding());
                panel.accentLeft(accent, style.accentWidth());
                panel.keepTogether();
                ctx.renderBlocks(node.content(), panel);
            });
        }

        private static DocumentColor accentFor(String variant, DocumentColor fallback) {
            if (variant == null) {
                return fallback;
            }
            switch (variant.toLowerCase()) {
                case "warning":
                case "caution":
                    return DocumentColor.rgb(217, 119, 6);
                case "info":
                case "note":
                    return DocumentColor.rgb(37, 99, 235);
                case "error":
                case "danger":
                    return DocumentColor.rgb(220, 38, 38);
                case "success":
                case "tip":
                    return DocumentColor.rgb(22, 163, 74);
                default:
                    return fallback;
            }
        }
    }
}
