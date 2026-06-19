package io.github.demchaav.markdown.extension;

import java.util.Optional;

/**
 * Resolves a Markdown image source to raw image bytes.
 *
 * <p>This is the extension seam for image loading. The
 * {@link DefaultImageResolver} resolves local files and classpath resources;
 * remote URLs are not fetched by default for safety. Supply a custom resolver
 * (e.g. one that fetches {@code https} URLs, or pulls from a content store) via
 * the theme.</p>
 */
@FunctionalInterface
public interface ImageResolver {

    /**
     * Resolves an image source to its bytes.
     *
     * @param source the source string from the Markdown ({@code ![alt](source)})
     * @return the image bytes, or empty if the source cannot be resolved
     */
    Optional<byte[]> resolve(String source);
}
