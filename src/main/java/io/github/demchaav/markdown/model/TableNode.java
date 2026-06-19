package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * A GFM table: a header row, body rows, and per-column alignment.
 *
 * @param alignments per-column alignment (size = column count)
 * @param header     the header row's cells
 * @param bodyRows   the body rows, each a list of cells
 */
public record TableNode(List<ColumnAlignment> alignments,
                        List<TableCellNode> header,
                        List<List<TableCellNode>> bodyRows) implements MarkdownNode {

    /**
     * Creates a table node.
     *
     * @param alignments per-column alignment; copied defensively, must not be {@code null}
     * @param header     the header cells; copied defensively, must not be {@code null}
     * @param bodyRows   the body rows; copied defensively (deeply), must not be {@code null}
     */
    public TableNode {
        alignments = List.copyOf(Objects.requireNonNull(alignments, "alignments"));
        header = List.copyOf(Objects.requireNonNull(header, "header"));
        Objects.requireNonNull(bodyRows, "bodyRows");
        bodyRows = bodyRows.stream().map(List::copyOf).toList();
    }

    /** @return the number of columns (from the alignment list) */
    public int columnCount() {
        return alignments.size();
    }
}
