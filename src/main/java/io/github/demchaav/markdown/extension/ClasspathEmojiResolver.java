package io.github.demchaav.markdown.extension;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

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

    /** Valid emoji shortcode characters — also blocks {@code /}, {@code .} and {@code ..}. */
    private static final Pattern SHORTCODE = Pattern.compile("[A-Za-z0-9_+-]+");

    /** Cap the cache so adversarial input (every {@code :word:} is an emoji) can't grow it forever. */
    private static final int MAX_CACHE = 4096;

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
        // Reject blanks and anything that is not a plain shortcode — in particular '/', '.'
        // and '..', so untrusted Markdown can't walk the classpath via the resource path.
        if (shortcode == null || !SHORTCODE.matcher(shortcode).matches()) {
            return Optional.empty();
        }
        Optional<byte[]> cached = cache.get(shortcode);
        if (cached != null) {
            return cached;
        }
        Optional<byte[]> loaded = load(shortcode);
        if (cache.size() < MAX_CACHE) {
            cache.putIfAbsent(shortcode, loaded);
        }
        return loaded;
    }

    private Optional<byte[]> load(String shortcode) {
        String resource = basePath + shortcode + ".png";
        // Prefer the thread-context classloader (where a web-app / module's assets usually
        // live), then fall back to this class's own loader.
        Optional<byte[]> fromContext = read(Thread.currentThread().getContextClassLoader(), resource);
        return fromContext.isPresent()
                ? fromContext
                : read(ClasspathEmojiResolver.class.getClassLoader(), resource);
    }

    private static Optional<byte[]> read(ClassLoader loader, String resource) {
        if (loader == null) {
            return Optional.empty();
        }
        try (InputStream in = loader.getResourceAsStream(resource)) {
            return in == null ? Optional.empty() : Optional.of(in.readAllBytes());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
