package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.ParagraphBuilder;
import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.node.TextAlign;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentStroke;
import com.demcha.compose.document.table.DocumentTableCell;
import com.demcha.compose.document.table.DocumentTableColumn;
import com.demcha.compose.document.table.DocumentTableStyle;
import com.demcha.compose.document.table.DocumentTableTextAnchor;
import io.github.demchaav.markdown.model.ColumnAlignment;
import io.github.demchaav.markdown.model.TableCellNode;
import io.github.demchaav.markdown.model.TableNode;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;

import java.util.ArrayList;
import java.util.List;

/** Renders a GFM table with even fixed columns, a styled header row, and per-column alignment. */
public final class TableRenderer implements NodeRenderer<TableNode> {

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
            // Cell paragraphs bypass ParagraphRenderer, so place the footnote back-anchor here
            // too — otherwise a footnote first cited in a table cell has a dead back-link.
            // anchor(null) is a no-op on the builder.
            String backAnchor = ctx.footnoteBackAnchor(content);
            var paragraph = new ParagraphBuilder().rich(rich).align(textAlign(alignment))
                    .anchor(backAnchor).build();
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
                base.codeFamily(), base.codeSize(), base.codeColor(), base.codeBackground(),
                base.linkColor(), base.underlineLinks());
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
