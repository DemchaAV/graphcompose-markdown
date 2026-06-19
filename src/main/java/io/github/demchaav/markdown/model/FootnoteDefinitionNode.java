package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * A single footnote definition: its number and its block content.
 *
 * @param number  the 1-based footnote number
 * @param content the block content of the definition (usually one paragraph)
 */
public record FootnoteDefinitionNode(int number, List<MarkdownNode> content) {

    /**
     * Creates a footnote definition.
     *
     * @param number  the footnote number
     * @param content the block content; copied defensively, must not be {@code null}
     */
    public FootnoteDefinitionNode {
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }
}
