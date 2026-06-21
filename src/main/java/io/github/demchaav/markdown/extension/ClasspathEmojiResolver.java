package io.github.demchaav.markdown.extension;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An {@link EmojiResolver} that loads emoji PNGs from the classpath — one file per
 * shortcode at {@code <basePath>/<shortcode>.png}. Lookups are cached (including
 * misses), so it is cheap to reuse and safe to share across threads.
 *
 * <p>Drop a set of emoji images on your classpath (e.g. Twemoji PNGs renamed by
 * shortcode) and point a theme at this resolver to render inline emoji with no network
 * access:</p>
 *
 * <pre>{@code
 * MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
 *         .emojiResolver(new ClasspathEmojiResolver("emoji"))   // /emoji/rocket.png, …
 *         .build();
 * }</pre>
 *
 * <p>A shortcode with no matching file resolves to empty, so it falls back to its
 * literal {@code :shortcode:} text.</p>
 */
public final class ClasspathEmojiResolver implements EmojiResolver {

    private final String basePath;
    private final ConcurrentHashMap<String, Optional<byte[]>> cache = new ConcurrentHashMap<>();

    /**
     * Creates a resolver rooted at a classpath directory.
     *
     * @param basePath the classpath directory holding {@code <shortcode>.png} files
     *                 (e.g. {@code "emoji"} or {@code "twemoji/72x72"})
     */
    public ClasspathEmojiResolver(String basePath) {
        String trimmed = basePath == null ? "" : basePath.strip();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (!trimmed.isEmpty() && !trimmed.endsWith("/")) {
            trimmed += "/";
        }
        this.basePath = trimmed;
    }

    @Override
    public Optional<byte[]> resolve(String shortcode) {
        if (shortcode == null || shortcode.isBlank()) {
            return Optional.empty();
        }
        return cache.computeIfAbsent(shortcode, this::load);
    }

    private Optional<byte[]> load(String shortcode) {
        String resource = "/" + basePath + shortcode + ".png";
        try (InputStream in = ClasspathEmojiResolver.class.getResourceAsStream(resource)) {
            return in == null ? Optional.empty() : Optional.of(in.readAllBytes());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
