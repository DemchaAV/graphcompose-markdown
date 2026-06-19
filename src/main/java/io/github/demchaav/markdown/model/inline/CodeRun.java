package io.github.demchaav.markdown.model.inline;

import java.util.Objects;

/**
 * Inline code (`code`) — literal text rendered in the monospace code style.
 *
 * @param text the literal code text
 */
public record CodeRun(String text) implements InlineNode {

    /**
     * Creates an inline code run.
     *
     * @param text the literal code text; must not be {@code null}
     */
    public CodeRun {
        Objects.requireNonNull(text, "text");
    }
}
