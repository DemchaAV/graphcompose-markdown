package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.packs.AcademicTheme;
import io.github.demchaav.markdown.theme.packs.BusinessReportTheme;
import io.github.demchaav.markdown.theme.packs.GitHubTheme;
import io.github.demchaav.markdown.theme.packs.MinimalTheme;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renders the same Markdown through every bundled theme — the reskinning showcase.
 * Writes one PDF per theme into a {@code gallery/} directory.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.ThemeGalleryExample
 * </pre>
 */
public final class ThemeGalleryExample {

    private ThemeGalleryExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown = bundledSample();

        Map<String, MarkdownTheme> themes = new LinkedHashMap<>();
        themes.put("default-light", DefaultMarkdownTheme.light());
        themes.put("default-dark", DefaultMarkdownTheme.dark());
        themes.put("github-light", GitHubTheme.light());
        themes.put("github-dark", GitHubTheme.dark());
        themes.put("academic", AcademicTheme.light());
        themes.put("minimal", MinimalTheme.light());
        themes.put("business-report", BusinessReportTheme.light());

        Path outDir = Path.of("gallery");
        Files.createDirectories(outDir);

        for (Map.Entry<String, MarkdownTheme> entry : themes.entrySet()) {
            Path out = outDir.resolve(entry.getKey() + ".pdf");
            MarkdownComposer.create(entry.getValue()).render(markdown).writePdf(out);
            System.out.println("Wrote " + out);
        }
    }

    private static String bundledSample() throws Exception {
        try (InputStream in = ThemeGalleryExample.class.getResourceAsStream("/sample.md")) {
            if (in == null) {
                throw new IllegalStateException("bundled sample.md not found on the classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
