package io.github.demchaav.markdown.extension;

import java.util.Optional;

/**
 * Resolves an emoji shortcode (e.g. {@code "smile"}) to inline image bytes (PNG), or an
 * empty result to fall back to the literal {@code :shortcode:} text.
 *
 * <p>This is the pluggable seam for emoji rendering. The default ({@link #none()})
 * resolves nothing, so emoji shortcodes render as readable text rather than the broken
 * glyphs a PDF font would produce. Plug a resolver that returns PNG bytes (Twemoji from
 * the classpath or a network CDN) to render real inline images. When the engine later
 * gains inline-SVG support, an SVG-based strategy can drop in behind this same seam
 * without touching the rest of the pipeline.</p>
 */
@FunctionalInterface
public interface EmojiResolver {

    /**
     * Resolves a shortcode to inline PNG image bytes.
     *
     * @param shortcode the emoji shortcode without colons (e.g. {@code "rocket"})
     * @return the PNG bytes, or {@link Optional#empty()} to render the shortcode as text
     */
    Optional<byte[]> resolve(String shortcode);

    /**
     * Returns a resolver that resolves nothing — every emoji renders as its literal
     * {@code :shortcode:} text. This is the default.
     *
     * @return a no-op resolver
     */
    static EmojiResolver none() {
        return shortcode -> Optional.empty();
    }
}
