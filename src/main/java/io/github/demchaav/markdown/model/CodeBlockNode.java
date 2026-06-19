package io.github.demchaav.markdown.model;

import java.util.Objects;

/**
 * A fenced or indented code block.
 *
 * @param language the info string / language hint (e.g. {@code "java"}); empty if none
 * @param code     the literal code text, with the trailing newline stripped
 */
public record CodeBlockNode(String language, String code) implements MarkdownNode {

    /**
     * Creates a code block node.
     *
     * @param language the language hint; must not be {@code null} (use {@code ""} if absent)
     * @param code     the literal code text; must not be {@code null}
     */
    public CodeBlockNode {
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(code, "code");
    }
}
