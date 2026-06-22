package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * An ordered or unordered list.
 *
 * @param ordered     {@code true} for an ordered (numbered) list, {@code false} for a bullet list
 * @param startNumber the first number of an ordered list (usually 1); ignored for bullet lists
 * @param items       the list items
 * @param loose       {@code true} for a loose list (items separated by blank lines, rendered with
 *                    extra inter-item spacing); {@code false} for a tight list
 */
public record ListNode(boolean ordered, int startNumber, List<ListItemNode> items, boolean loose)
        implements MarkdownNode {

    /**
     * Creates a list node.
     *
     * @param ordered     whether the list is ordered
     * @param startNumber the starting number for an ordered list
     * @param items       the items; copied defensively, must not be {@code null}
     * @param loose       whether the list is loose
     */
    public ListNode {
        items = List.copyOf(Objects.requireNonNull(items, "items"));
    }

    /**
     * Creates a tight list node ({@code loose = false}).
     *
     * @param ordered     whether the list is ordered
     * @param startNumber the starting number for an ordered list
     * @param items       the items; copied defensively, must not be {@code null}
     */
    public ListNode(boolean ordered, int startNumber, List<ListItemNode> items) {
        this(ordered, startNumber, items, false);
    }
}
