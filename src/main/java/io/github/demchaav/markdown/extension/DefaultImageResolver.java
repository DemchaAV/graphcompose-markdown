package io.github.demchaav.markdown.extension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Default {@link ImageResolver}: resolves local file paths and classpath
 * resources only.
 *
 * <p>Resolution order: an absolute or base-relative file path, then a classpath
 * resource. Remote {@code http}/{@code https} URLs are intentionally not
 * fetched — supply a custom resolver if you need network loading, so that
 * fetching is an explicit, opt-in decision.</p>
 */
public final class DefaultImageResolver implements ImageResolver {

    private final Path baseDir;

    /** Creates a resolver that interprets relative paths against the working directory. */
    public DefaultImageResolver() {
        this(null);
    }

    /**
     * Creates a resolver that interprets relative paths against a base directory.
     *
     * @param baseDir the base directory for relative file paths; may be {@code null}
     */
    public DefaultImageResolver(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public Optional<byte[]> resolve(String source) {
        if (source == null || source.isBlank()) {
            return Optional.empty();
        }
        String lower = source.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return Optional.empty(); // remote loading is opt-in via a custom resolver
        }
        Optional<byte[]> fromFile = readFile(source);
        if (fromFile.isPresent()) {
            return fromFile;
        }
        return readClasspath(source);
    }

    private Optional<byte[]> readFile(String source) {
        try {
            Path path = baseDir == null ? Path.of(source) : baseDir.resolve(source);
            if (Files.isRegularFile(path)) {
                return Optional.of(Files.readAllBytes(path));
            }
        } catch (IOException | RuntimeException ignored) {
            // fall through to classpath
        }
        return Optional.empty();
    }

    private Optional<byte[]> readClasspath(String source) {
        String resource = source.startsWith("/") ? source.substring(1) : source;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = DefaultImageResolver.class.getClassLoader();
        }
        try (InputStream in = loader.getResourceAsStream(resource)) {
            if (in != null) {
                return Optional.of(in.readAllBytes());
            }
        } catch (IOException ignored) {
            // not found / unreadable
        }
        return Optional.empty();
    }
}
