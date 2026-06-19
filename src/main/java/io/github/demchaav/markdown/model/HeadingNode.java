package io.github.demchaav.markdown.model;

import io.github.demchaav.markdown.model.inline.InlineNode;

import java.util.List;
import java.util.Objects;

/**
 * A heading (h1–h6).
 *
 * @param level   the heading level, 1 through 6
 * @param content the inline content of the heading
 */
public record HeadingNode(int level, List<InlineNode> content) implements MarkdownNode {

    /**
     * Creates a heading node.
     *
     * @param level   the heading level; must be between 1 and 6 inclusive
     * @param content the inline content; copied defensively, must not be {@code null}
     * @throws IllegalArgumentException if {@code level} is outside 1–6
     */
    public HeadingNode {
        if (level < 1 || level > 6) {
            throw new IllegalArgumentException("Heading level must be 1..6, was " + level);
        }
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }
}
