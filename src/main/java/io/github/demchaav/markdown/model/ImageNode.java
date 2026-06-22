package io.github.demchaav.markdown.model;

import java.util.Objects;

/**
 * A block-level image — an image that is the sole content of its paragraph.
 *
 * <p>The {@code title} is captured for programmatic use but is not currently rendered
 * (PDF has no native image-tooltip concept).</p>
 *
 * @param source the image source (path, classpath resource, or URL)
 * @param alt    the alternate text; may be empty
 * @param title  the optional image title; may be {@code null}
 */
public record ImageNode(String source, String alt, String title) implements MarkdownNode {

    /**
     * Creates a block image node.
     *
     * @param source the image source; must not be {@code null}
     * @param alt    the alternate text; must not be {@code null} (use {@code ""} if absent)
     * @param title  the optional image title; may be {@code null}
     */
    public ImageNode {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(alt, "alt");
    }
}
