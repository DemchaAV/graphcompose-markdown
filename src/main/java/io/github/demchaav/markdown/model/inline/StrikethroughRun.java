package io.github.demchaav.markdown.model.inline;

import java.util.List;
import java.util.Objects;

/**
 * Strikethrough ({@code ~~text~~}) wrapping further inline content.
 *
 * @param children the inline content rendered with a line through it
 */
public record StrikethroughRun(List<InlineNode> children) implements InlineNode {

    /**
     * Creates a strikethrough run.
     *
     * @param children the wrapped inline content; copied defensively, must not be {@code null}
     */
    public StrikethroughRun {
        children = List.copyOf(Objects.requireNonNull(children, "children"));
    }
}
