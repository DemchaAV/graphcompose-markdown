package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.extension.ClasspathEmojiResolver;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;

import java.nio.file.Path;

/**
 * Emoji shortcodes ({@code :rocket:}) rendered as inline images via a
 * {@link ClasspathEmojiResolver}.
 *
 * <p>The bundled {@code /emoji/*.png} are real Twemoji images (CC-BY 4.0), named by
 * shortcode. Add more by dropping {@code <shortcode>.png} files into
 * {@code examples/src/main/resources/emoji/}. A resolver image always wins; a shortcode
 * the resolver misses falls through to the vector Noto set ({@code graph-compose-emoji},
 * on this module's classpath — see {@code VectorEmojiExample}), and an entirely unknown
 * shortcode stays literal text rather than a broken glyph.</p>
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.EmojiExample
 * </pre>
 */
public final class EmojiExample {

    private EmojiExample() {
    }

    public static void main(String[] args) throws Exception {
        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .emojiResolver(new ClasspathEmojiResolver("emoji"))
                .build();

        String markdown = """
                # Emoji

                Ship it :rocket: — tests pass :white_check_mark: and the crowd goes :tada:.
                This release is on :fire:, give it a :star:.

                A shortcode the resolver misses, like :shrug:, falls through to the
                vector Noto set; an entirely unknown one, like :definitelynotanemoji:,
                stays literal text.
                """;

        Path out = Path.of("emoji.pdf");
        MarkdownComposer.create(theme).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath());
    }
}
