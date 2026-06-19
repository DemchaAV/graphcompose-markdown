package io.github.demchaav.markdown.model.inline;

import java.util.List;
import java.util.Objects;

/**
 * Strong emphasis (bold) wrapping further inline content.
 *
 * @param children the inline content rendered bold
 */
public record StrongRun(List<InlineNode> children) implements InlineNode {

    /**
     * Creates a strong (bold) run.
     *
     * @param children the wrapped inline content; copied defensively, must not be {@code null}
     */
    public StrongRun {
        children = List.copyOf(Objects.requireNonNull(children, "children"));
    }
}
