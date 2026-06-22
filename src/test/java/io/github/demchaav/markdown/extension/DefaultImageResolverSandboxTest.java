package io.github.demchaav.markdown.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The opt-in sandbox ({@link DefaultImageResolver#sandboxed}) confines filesystem resolution
 * to its base directory, so untrusted Markdown cannot read arbitrary local files via an
 * absolute path or a {@code ../} escape. Each escape test contrasts with the unsandboxed
 * default, which deliberately still reads them.
 */
class DefaultImageResolverSandboxTest {

    @Test
    void sandboxRejectsAnAbsolutePathOutsideBaseDir(@TempDir Path baseDir, @TempDir Path elsewhere)
            throws IOException {
        Path secret = elsewhere.resolve("secret.png");
        Files.write(secret, new byte[] {9, 9, 9});
        String absolute = secret.toAbsolutePath().toString();

        assertThat(DefaultImageResolver.sandboxed(baseDir).resolve(absolute)).isEmpty();
        // The unsandboxed default DOES read it — documents the behaviour the sandbox closes.
        assertThat(new DefaultImageResolver(baseDir).resolve(absolute)).isPresent();
    }

    @Test
    void sandboxRejectsAParentTraversal(@TempDir Path baseDir) throws IOException {
        Path secret = baseDir.getParent().resolve("up-secret-xyz.png");
        Files.write(secret, new byte[] {7});
        try {
            String escape = "../" + secret.getFileName();
            assertThat(DefaultImageResolver.sandboxed(baseDir).resolve(escape)).isEmpty();
            assertThat(new DefaultImageResolver(baseDir).resolve(escape)).isPresent(); // default still reads it
        } finally {
            Files.deleteIfExists(secret);
        }
    }

    @Test
    void sandboxAllowsFilesWithinBaseDir(@TempDir Path baseDir) throws IOException {
        Files.write(baseDir.resolve("ok.png"), new byte[] {1, 2});
        Path sub = Files.createDirectories(baseDir.resolve("sub"));
        Files.write(sub.resolve("nested.png"), new byte[] {3, 4});

        assertThat(DefaultImageResolver.sandboxed(baseDir).resolve("ok.png")).isPresent();
        assertThat(DefaultImageResolver.sandboxed(baseDir).resolve("sub/nested.png")).isPresent();
    }

    @Test
    void sandboxStillResolvesClasspathResources(@TempDir Path baseDir) {
        // Sandboxing constrains the filesystem only; bundled app resources still resolve.
        assertThat(DefaultImageResolver.sandboxed(baseDir).resolve("emoji/rocket.png")).isPresent();
    }

    @Test
    void sandboxedRequiresABaseDir() {
        assertThatThrownBy(() -> DefaultImageResolver.sandboxed(null))
                .isInstanceOf(NullPointerException.class);
    }
}
