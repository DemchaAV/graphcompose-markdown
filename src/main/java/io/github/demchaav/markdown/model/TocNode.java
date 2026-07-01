package io.github.demchaav.markdown.model;

/**
 * A table-of-contents marker block. A standalone {@code [TOC]} (or {@code [[_TOC_]]}) line in
 * the Markdown maps to this node; the renderer replaces it with an auto-generated, clickable
 * list of links to every heading in the document, nested by heading level.
 *
 * <p>The node carries no data — the heading list is derived from the document at render time,
 * so a {@code [TOC]} at the top of a file correctly lists headings that appear below it.</p>
 */
public record TocNode() implements MarkdownNode {
}
