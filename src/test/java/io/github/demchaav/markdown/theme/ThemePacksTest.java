package io.github.demchaav.markdown.theme;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.packs.AcademicTheme;
import io.github.demchaav.markdown.theme.packs.BusinessReportTheme;
import io.github.demchaav.markdown.theme.packs.GitHubTheme;
import io.github.demchaav.markdown.theme.packs.MinimalTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ThemePacksTest {

    private static final String SAMPLE = """
            # Quarterly Review

            A **themeable** Markdown composer — the same content, *different* themes.

            - Revenue up
            - Costs down
            - [Details](https://example.com)

            > Design tokens drive the cosmetics; renderers drive the behaviour.

            | Metric  |  Q3 |  Q4 |
            |:--------|----:|----:|
            | Revenue | 120 | 140 |
            | Margin  | 18% | 22% |

            ```java
            MarkdownTheme theme = GitHubTheme.light();
            ```

            :::callout tip
            Derive a pack from the defaults and override only what differs.
            :::
            """;

    private static Map<String, MarkdownTheme> packs() {
        Map<String, MarkdownTheme> map = new LinkedHashMap<>();
        map.put("default-light", DefaultMarkdownTheme.light());
        map.put("github-light", GitHubTheme.light());
        map.put("github-dark", GitHubTheme.dark());
        map.put("academic", AcademicTheme.light());
        map.put("minimal", MinimalTheme.light());
        map.put("business-report", BusinessReportTheme.light());
        return map;
    }

    @Test
    void everyPackRendersTheSameContentToPdf() throws Exception {
        Path outDir = Path.of("target", "generated-pdfs", "themes");
        Files.createDirectories(outDir);

        for (Map.Entry<String, MarkdownTheme> entry : packs().entrySet()) {
            byte[] pdf = MarkdownComposer.create(entry.getValue()).render(SAMPLE).toPdfBytes();
            String header = new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.US_ASCII);
            assertThat(header).as("pack %s renders a PDF", entry.getKey()).isEqualTo("%PDF-");

            Path pdfFile = outDir.resolve(entry.getKey() + ".pdf");
            Files.write(pdfFile, pdf);
            renderFirstPagePng(pdfFile, outDir.resolve(entry.getKey() + ".png"));
        }
    }

    private static void renderFirstPagePng(Path pdf, Path png) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            BufferedImage image = new PDFRenderer(doc).renderImageWithDPI(0, 110);
            ImageIO.write(image, "PNG", png.toFile());
        }
    }
}
