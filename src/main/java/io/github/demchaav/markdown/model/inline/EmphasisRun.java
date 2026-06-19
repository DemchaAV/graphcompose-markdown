package io.github.demchaav.markdown.model.inline;

import java.util.List;
import java.util.Objects;

/**
 * Emphasis (italic) wrapping further inline content.
 *
 * @param children the inline content rendered italic
 */
public record EmphasisRun(List<InlineNode> children) implements InlineNode {

    /**
     * Creates an emphasis (italic) run.
     *
     * @param children the wrapped inline content; copied defensively, must not be {@code null}
     */
    public EmphasisRun {
        children = List.copyOf(Objects.requireNonNull(children, "children"));
    }
}
