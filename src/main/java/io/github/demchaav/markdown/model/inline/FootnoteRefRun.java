package io.github.demchaav.markdown.model.inline;

/**
 * An inline footnote reference ({@code [^1]}) — carries the resolved footnote
 * number; the matching definition is collected into a document-end
 * {@link io.github.demchaav.markdown.model.FootnotesNode}.
 *
 * @param number the 1-based footnote number
 */
public record FootnoteRefRun(int number) implements InlineNode {
}
