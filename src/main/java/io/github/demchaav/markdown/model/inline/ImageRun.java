package io.github.demchaav.markdown.model.inline;

import java.util.Objects;

/**
 * An inline image.
 *
 * <p>An image that is the sole content of a paragraph is represented as a
 * block-level {@link io.github.demchaav.markdown.model.ImageNode} instead; this
 * run is for images that sit amid other inline content.</p>
 *
 * <p>The {@code title} is captured for programmatic use but is not currently rendered
 * (PDF has no native image-tooltip concept).</p>
 *
 * @param source the image source (path, classpath resource, or URL)
 * @param alt    the alternate text; may be empty
 * @param title  the optional image title; may be {@code null}
 */
public record ImageRun(String source, String alt, String title) implements InlineNode {

    /**
     * Creates an inline image run.
     *
     * @param source the image source; must not be {@code null}
     * @param alt    the alternate text; must not be {@code null} (use {@code ""} if absent)
     * @param title  the optional image title; may be {@code null}
     */
    public ImageRun {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(alt, "alt");
    }
}
