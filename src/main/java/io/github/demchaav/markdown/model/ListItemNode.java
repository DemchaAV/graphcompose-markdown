package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * A single item of a {@link ListNode}.
 *
 * <p>An item holds block content so that loose lists, multi-paragraph items and
 * nested lists (a {@link ListNode} among the children) are all representable.</p>
 *
 * @param content the block content of the item
 */
public record ListItemNode(List<MarkdownNode> content) {

    /**
     * Creates a list item node.
     *
     * @param content the block content; copied defensively, must not be {@code null}
     */
    public ListItemNode {
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }
}
