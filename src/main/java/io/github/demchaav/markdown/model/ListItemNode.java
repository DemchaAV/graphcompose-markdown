package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * A single item of a {@link ListNode}.
 *
 * <p>An item holds block content so that loose lists, multi-paragraph items and
 * nested lists (a {@link ListNode} among the children) are all representable. The
 * {@code checked} flag distinguishes GFM task-list items: {@code null} for a plain
 * item, {@code TRUE} for {@code [x]}, {@code FALSE} for {@code [ ]}.</p>
 *
 * @param content the block content of the item
 * @param checked task-list checkbox state, or {@code null} if not a task item
 */
public record ListItemNode(List<MarkdownNode> content, Boolean checked) {

    /**
     * Creates a list item node.
     *
     * @param content the block content; copied defensively, must not be {@code null}
     * @param checked task-list checkbox state, or {@code null} if not a task item
     */
    public ListItemNode {
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }

    /**
     * Creates a plain (non-task) list item.
     *
     * @param content the block content
     */
    public ListItemNode(List<MarkdownNode> content) {
        this(content, null);
    }

    /** @return {@code true} if this is a GFM task-list item ({@code [ ]}/{@code [x]}) */
    public boolean isTask() {
        return checked != null;
    }
}
