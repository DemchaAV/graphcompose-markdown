package io.github.demchaav.markdown.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The default image resolver resolves local files and classpath resources only. In
 * particular, remote {@code http}/{@code https} URLs are intentionally NOT fetched — a
 * safety contract for untrusted Markdown that deserves a guard test so a refactor can't
 * silently start making network requests.
 */
class DefaultImageResolverTest {

    private final DefaultImageResolver resolver = new DefaultImageResolver();

    @Test
    void nullOrBlankSourceResolvesToEmpty() {
        assertThat(resolver.resolve(null)).isEmpty();
        assertThat(resolver.resolve("")).isEmpty();
        assertThat(resolver.resolve("   ")).isEmpty();
    }

    @Test
    void remoteUrlsAreNotFetched() {
        // The safety contract: a remote URL is never fetched by the default resolver.
        assertThat(resolver.resolve("http://example.com/logo.png")).isEmpty();
        assertThat(resolver.resolve("https://example.com/logo.png")).isEmpty();
        assertThat(resolver.resolve("HTTPS://EXAMPLE.COM/LOGO.PNG")).isEmpty(); // case-insensitive
    }

    @Test
    void resolvesAClasspathResource() {
        // /emoji/rocket.png ships under src/test/resources, so it is on the test classpath.
        assertThat(resolver.resolve("emoji/rocket.png")).isPresent();
        assertThat(resolver.resolve("/emoji/rocket.png")).isPresent(); // leading slash is stripped
    }

    @Test
    void resolvesARealFileUnderTheBaseDir(@TempDir Path dir) throws IOException {
        Path image = dir.resolve("pic.png");
        Files.write(image, new byte[] {1, 2, 3, 4});
        DefaultImageResolver based = new DefaultImageResolver(dir);

        assertThat(based.resolve("pic.png")).isPresent();
        assertThat(based.resolve("pic.png").orElseThrow()).containsExactly(1, 2, 3, 4);
    }

    @Test
    void missingSourceResolvesToEmpty() {
        assertThat(resolver.resolve("no/such/image-xyz.png")).isEmpty();
    }
}
