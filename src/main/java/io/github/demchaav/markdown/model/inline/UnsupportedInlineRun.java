package io.github.demchaav.markdown.model.inline;

import java.util.Objects;

/**
 * Inline content the mapper does not model — raw inline HTML other than a line break,
 * or any future inline type that is not yet handled.
 *
 * <p>The original source is kept so it is surfaced literally rather than silently
 * dropped; strict mode rejects a document that contains one.</p>
 *
 * @param raw the original inline source (e.g. {@code "<sub>"})
 */
public record UnsupportedInlineRun(String raw) implements InlineNode {

    /**
     * Creates an unsupported-inline run.
     *
     * @param raw the original inline source; must not be {@code null}
     */
    public UnsupportedInlineRun {
        Objects.requireNonNull(raw, "raw");
    }
}
