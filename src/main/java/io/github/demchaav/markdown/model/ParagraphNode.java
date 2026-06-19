package io.github.demchaav.markdown.model;

import io.github.demchaav.markdown.model.inline.InlineNode;

import java.util.List;
import java.util.Objects;

/**
 * A paragraph of inline content.
 *
 * @param content the inline content of the paragraph
 */
public record ParagraphNode(List<InlineNode> content) implements MarkdownNode {

    /**
     * Creates a paragraph node.
     *
     * @param content the inline content; copied defensively, must not be {@code null}
     */
    public ParagraphNode {
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }
}
