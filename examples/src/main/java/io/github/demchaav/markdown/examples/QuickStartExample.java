package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;

import java.nio.file.Path;

/**
 * The 30-second quickstart: an inline Markdown string to a themed PDF.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.QuickStartExample
 * </pre>
 */
public final class QuickStartExample {

    private QuickStartExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown = """
                # Quickstart

                Render **Markdown** to a *themed* PDF in three lines.

                - Headings, lists and `inline code`
                - Syntax-highlighted code blocks
                - [Links](https://github.com/DemchaAV/graphcompose-markdown)

                > Themes decide how all of this looks.
                """;

        Path out = Path.of("quickstart.pdf");
        MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render(markdown)
                .writePdf(out);

        System.out.println("Wrote " + out.toAbsolutePath());
    }
}
