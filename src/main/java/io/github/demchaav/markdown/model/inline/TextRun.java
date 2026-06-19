package io.github.demchaav.markdown.model.inline;

import java.util.Objects;

/**
 * A leaf run of literal text with no inline decoration of its own.
 *
 * @param text the literal text (already unescaped)
 */
public record TextRun(String text) implements InlineNode {

    /**
     * Creates a text run.
     *
     * @param text the literal text; must not be {@code null}
     */
    public TextRun {
        Objects.requireNonNull(text, "text");
    }
}
