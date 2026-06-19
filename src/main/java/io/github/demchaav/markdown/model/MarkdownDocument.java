package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * The root of the semantic Markdown tree — an ordered list of block nodes.
 *
 * <p>This is the library's own model, independent of the Flexmark AST it is
 * mapped from and of the GraphCompose document it is rendered into. It is the
 * stable hand-off point between parsing and theming.</p>
 *
 * @param blocks the top-level block nodes, in document order
 */
public record MarkdownDocument(List<MarkdownNode> blocks) {

    /**
     * Creates a Markdown document.
     *
     * @param blocks the top-level block nodes; copied defensively, must not be {@code null}
     */
    public MarkdownDocument {
        blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks"));
    }
}
