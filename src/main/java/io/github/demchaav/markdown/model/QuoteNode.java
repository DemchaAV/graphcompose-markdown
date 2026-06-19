package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * A block quote, holding the block content nested inside it.
 *
 * @param content the quoted block content (may itself contain quotes, lists, etc.)
 */
public record QuoteNode(List<MarkdownNode> content) implements MarkdownNode {

    /**
     * Creates a quote node.
     *
     * @param content the quoted block content; copied defensively, must not be {@code null}
     */
    public QuoteNode {
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }
}
