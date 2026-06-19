package io.github.demchaav.markdown.model;

import io.github.demchaav.markdown.model.inline.InlineNode;

import java.util.List;
import java.util.Objects;

/**
 * A single table cell, holding inline content.
 *
 * @param content the inline content of the cell
 */
public record TableCellNode(List<InlineNode> content) {

    /**
     * Creates a table cell.
     *
     * @param content the inline content; copied defensively, must not be {@code null}
     */
    public TableCellNode {
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }
}
