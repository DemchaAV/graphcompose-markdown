package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * An ordered or unordered list.
 *
 * @param ordered     {@code true} for an ordered (numbered) list, {@code false} for a bullet list
 * @param startNumber the first number of an ordered list (usually 1); ignored for bullet lists
 * @param items       the list items
 */
public record ListNode(boolean ordered, int startNumber, List<ListItemNode> items) implements MarkdownNode {

    /**
     * Creates a list node.
     *
     * @param ordered     whether the list is ordered
     * @param startNumber the starting number for an ordered list
     * @param items       the items; copied defensively, must not be {@code null}
     */
    public ListNode {
        items = List.copyOf(Objects.requireNonNull(items, "items"));
    }
}
