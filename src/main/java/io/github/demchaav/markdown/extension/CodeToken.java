package io.github.demchaav.markdown.extension;

import java.util.Objects;

/**
 * A run of code text with a single {@link CodeTokenType}. The concatenation of a
 * highlighter's tokens equals the original code verbatim (including whitespace
 * and newlines), so the renderer can reconstruct the source exactly.
 *
 * @param text the token text
 * @param type the token kind
 */
public record CodeToken(String text, CodeTokenType type) {

    /**
     * Creates a code token.
     *
     * @param text the token text; must not be {@code null}
     * @param type the token kind; must not be {@code null}
     */
    public CodeToken {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(type, "type");
    }
}
