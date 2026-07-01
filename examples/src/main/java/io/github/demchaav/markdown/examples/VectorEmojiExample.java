package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;

import java.nio.file.Path;

/**
 * Vector colour emoji out of the box: with the optional {@code graph-compose-emoji} artifact on
 * the classpath (this module adds it), {@code :shortcode:} emoji render as crisp Noto vector
 * glyphs — no user-supplied images, sharp at any size. Unknown shortcodes still fall back to
 * readable {@code :text:}. A theme {@code EmojiResolver} (see {@code EmojiExample}) overrides
 * the vector set where it supplies an image.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.VectorEmojiExample
 * </pre>
 */
public final class VectorEmojiExample {

    private VectorEmojiExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown = """
                # Vector emoji

                Ship it :rocket: — tested :white_check_mark:, documented :memo:,
                loved :heart: and starred :star:.

                | Status | Meaning |
                |---|---|
                | :white_check_mark: | done |
                | :hourglass: | in progress |
                | :fire: | urgent |

                An unknown shortcode stays readable text: :definitelynotanemoji:.
                """;

        Path out = Path.of("vector-emoji.pdf");
        MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath() + "  (colour vector glyphs, no images supplied)");
    }
}
