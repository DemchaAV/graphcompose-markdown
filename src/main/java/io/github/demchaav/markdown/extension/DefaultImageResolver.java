package io.github.demchaav.markdown.extension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Default {@link ImageResolver}: resolves local file paths and classpath
 * resources only.
 *
 * <p>Resolution order: an absolute or base-relative file path, then a classpath
 * resource. Remote {@code http}/{@code https} URLs are intentionally not
 * fetched — supply a custom resolver if you need network loading, so that
 * fetching is an explicit, opt-in decision.</p>
 *
 * <p><strong>Untrusted Markdown.</strong> By default a file source is resolved as-is, so
 * an absolute path ({@code ![x](/etc/passwd)}) or a {@code ../} traversal escapes the base
 * directory — fine for your own documents, but a local-file-disclosure risk if the Markdown
 * comes from an untrusted source. For that case build a sandboxed resolver with
 * {@link #sandboxed(Path)} (or {@code new DefaultImageResolver(baseDir, true)}): it confines
 * filesystem resolution to {@code baseDir}, rejecting absolute paths and {@code ../} escapes.
 * Classpath resolution (your application's own bundled resources) is unaffected.</p>
 */
public final class DefaultImageResolver implements ImageResolver {

    private final Path baseDir;
    private final boolean restrictToBaseDir;

    /** Creates a resolver that interprets relative paths against the working directory. */
    public DefaultImageResolver() {
        this(null, false);
    }

    /**
     * Creates a resolver that interprets relative paths against a base directory. File
     * resolution is not sandboxed — absolute paths and {@code ../} traversals are honoured.
     *
     * @param baseDir the base directory for relative file paths; may be {@code null}
     */
    public DefaultImageResolver(Path baseDir) {
        this(baseDir, false);
    }

    /**
     * Creates a resolver, optionally sandboxed to {@code baseDir}.
     *
     * @param baseDir           the base directory for relative file paths; when
     *                          {@code restrictToBaseDir} is {@code true} and this is
     *                          {@code null}, the working directory is used
     * @param restrictToBaseDir if {@code true}, a file source that resolves outside
     *                          {@code baseDir} (an absolute path or a {@code ../} escape) is
     *                          rejected — use this for untrusted Markdown
     */
    public DefaultImageResolver(Path baseDir, boolean restrictToBaseDir) {
        this.restrictToBaseDir = restrictToBaseDir;
        this.baseDir = restrictToBaseDir && baseDir == null ? Path.of("") : baseDir;
    }

    /**
     * A resolver sandboxed to {@code baseDir}: filesystem resolution is confined to that
     * directory (absolute paths and {@code ../} escapes are rejected). The right choice when
     * rendering Markdown from an untrusted source.
     *
     * @param baseDir the directory file sources are confined to; must not be {@code null}
     * @return a sandboxed resolver
     */
    public static DefaultImageResolver sandboxed(Path baseDir) {
        return new DefaultImageResolver(Objects.requireNonNull(baseDir, "baseDir"), true);
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
            if (restrictToBaseDir && !isWithinBaseDir(path)) {
                return Optional.empty(); // escapes the sandbox: absolute path or ../ traversal
            }
            if (Files.isRegularFile(path)) {
                return Optional.of(Files.readAllBytes(path));
            }
        } catch (IOException | RuntimeException ignored) {
            // fall through to classpath
        }
        return Optional.empty();
    }

    /** Whether {@code candidate} stays inside {@code baseDir} once normalised (no {@code ../} escape). */
    private boolean isWithinBaseDir(Path candidate) {
        Path root = (baseDir == null ? Path.of("") : baseDir).toAbsolutePath().normalize();
        return candidate.toAbsolutePath().normalize().startsWith(root);
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
