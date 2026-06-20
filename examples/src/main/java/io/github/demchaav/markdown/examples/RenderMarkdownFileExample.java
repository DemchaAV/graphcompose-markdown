package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads a Markdown <em>file</em> and writes a PDF <em>file</em> — the most common use.
 *
 * <pre>
 *   # render an explicit file to an explicit output
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.RenderMarkdownFileExample \
 *     -Dexec.args="../README.md README.pdf"
 *
 *   # no arguments → renders the bundled sample.md to sample.pdf
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.RenderMarkdownFileExample
 * </pre>
 */
public final class RenderMarkdownFileExample {

    private RenderMarkdownFileExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown;
        Path output;

        if (args.length >= 1) {
            Path input = Path.of(args[0]);
            markdown = Files.readString(input, StandardCharsets.UTF_8);
            output = Path.of(args.length >= 2 ? args[1] : stripExtension(input) + ".pdf");
        } else {
            markdown = bundledSample();
            output = Path.of("sample.pdf");
        }

        MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render(markdown)
                .writePdf(output);

        System.out.println("Wrote " + output.toAbsolutePath());
    }

    private static String bundledSample() throws Exception {
        try (InputStream in = RenderMarkdownFileExample.class.getResourceAsStream("/sample.md")) {
            if (in == null) {
                throw new IllegalStateException("bundled sample.md not found on the classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String stripExtension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}
