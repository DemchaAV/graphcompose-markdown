package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;

import java.nio.file.Path;

/**
 * A YAML front-matter block ({@code ---} … {@code ---}) renders a title block — title,
 * subtitle, author and date — above the document body.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.FrontMatterExample
 * </pre>
 */
public final class FrontMatterExample {

    private FrontMatterExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown = """
                ---
                title: Quarterly Review
                subtitle: Engineering metrics, Q4 2026
                author: Jane Doe
                date: 2026-06-21
                ---

                ## Summary

                Revenue is up and the team shipped on time.

                - Shipped the new renderer
                - Cut release lead time in half
                """;

        Path out = Path.of("front-matter.pdf");
        MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath());
    }
}
