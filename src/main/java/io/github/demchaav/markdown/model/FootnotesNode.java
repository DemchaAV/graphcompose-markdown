package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * The document-end footnotes ("Notes") block — the collected definitions, in
 * number order. The mapper appends one of these to the document when it contains
 * at least one footnote definition.
 *
 * @param definitions the footnote definitions, ordered by number
 */
public record FootnotesNode(List<FootnoteDefinitionNode> definitions) implements MarkdownNode {

    /**
     * Creates a footnotes block.
     *
     * @param definitions the definitions; copied defensively, must not be {@code null}
     */
    public FootnotesNode {
        definitions = List.copyOf(Objects.requireNonNull(definitions, "definitions"));
    }
}
