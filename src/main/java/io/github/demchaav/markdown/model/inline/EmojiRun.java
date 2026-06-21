package io.github.demchaav.markdown.model.inline;

import java.util.Objects;

/**
 * An emoji shortcode ({@code :smile:}). {@code shortcode} is the name between the
 * colons (e.g. {@code "smile"}). A renderer resolves it to an inline image through an
 * {@code EmojiResolver}, or falls back to the literal {@code :shortcode:} text.
 *
 * @param shortcode the emoji name, without colons
 */
public record EmojiRun(String shortcode) implements InlineNode {

    /**
     * Creates an emoji run.
     *
     * @param shortcode the emoji name without colons; must not be {@code null}
     */
    public EmojiRun {
        Objects.requireNonNull(shortcode, "shortcode");
    }
}
