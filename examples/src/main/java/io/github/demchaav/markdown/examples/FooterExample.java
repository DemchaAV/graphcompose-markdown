package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.FooterTokens;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A running footer with page numbers. Footers are off by default (clean single-page / screen
 * output); enable one by deriving a theme with a {@link FooterTokens} — here the built-in
 * {@code pageNumbers()} preset renders a centred "Page N of M" whose {@code {page}}/{@code {pages}}
 * tokens the engine substitutes per page.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.FooterExample
 * </pre>
 */
public final class FooterExample {

    private FooterExample() {
    }

    public static void main(String[] args) throws Exception {
        // A multi-page document so the footer's page numbers are worth having.
        String markdown = "# Quarterly Report\n\n" + IntStream.range(0, 200)
                .mapToObj(i -> "Section paragraph " + i + " — filler prose spanning several pages.")
                .collect(Collectors.joining("\n\n"));

        MarkdownTheme base = DefaultMarkdownTheme.light();
        MarkdownTheme withFooter = MarkdownTheme.builder(base)
                .tokens(base.tokens().withFooter(FooterTokens.pageNumbers())) // "Page {page} of {pages}"
                .build();

        Path out = Path.of("footer.pdf");
        MarkdownComposer.create(withFooter).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath() + "  (every page carries a 'Page N of M' footer)");
    }
}
