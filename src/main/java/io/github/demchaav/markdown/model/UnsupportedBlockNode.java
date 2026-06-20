package io.github.demchaav.markdown.model;

import java.util.Objects;

/**
 * A block the mapper has no dedicated node for — a raw HTML block, an HTML comment,
 * or any future Flexmark block type that is not yet modelled.
 *
 * <p>Rather than drop it (silently losing content from a <em>document</em>, which is
 * dangerous), the mapper preserves the original source text here so a renderer can
 * surface it. In strict mode the composer rejects a document that contains one.</p>
 *
 * @param kind the source Flexmark node name (e.g. {@code "HtmlBlock"}), for diagnostics
 * @param raw  the original source text of the block
 */
public record UnsupportedBlockNode(String kind, String raw) implements MarkdownNode {

    /**
     * Creates an unsupported-block node.
     *
     * @param kind the source node name; must not be {@code null}
     * @param raw  the original source text; must not be {@code null}
     */
    public UnsupportedBlockNode {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(raw, "raw");
    }
}
