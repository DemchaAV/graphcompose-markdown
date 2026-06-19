package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.extension.BundledFonts;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the canonical {@code master.md} showcase — the document that exercises the
 * whole parser — with the light, dark and JetBrains-Mono themes into the gitignored
 * {@code preview/} directory so the output can be eyeballed. Doubles as an end-to-end
 * smoke test that the showcase content renders.
 */
class SamplePdfGenerationTest {

    @Test
    void writesSamplePdfs() throws Exception {
        String sample = load("master.md");

        Path outDir = Path.of("preview");
        Files.createDirectories(outDir);

        Path light = outDir.resolve("markdown-sample-light.pdf");
        Path dark = outDir.resolve("markdown-sample-dark.pdf");
        Path jetbrains = outDir.resolve("markdown-sample-jetbrains.pdf");

        MarkdownComposer.create(DefaultMarkdownTheme.light()).render(sample).writePdf(light);
        MarkdownComposer.create(DefaultMarkdownTheme.dark()).render(sample).writePdf(dark);

        // Opt-in rich fonts — same light theme but code rendered in JetBrains Mono.
        MarkdownTheme jbTheme = BundledFonts.jetBrainsMonoCode(DefaultMarkdownTheme.light());
        MarkdownComposer.create(jbTheme).render(sample).writePdf(jetbrains);

        assertThat(light).exists();
        assertThat(dark).exists();
        assertThat(jetbrains).exists();
        assertThat(Files.size(light)).isGreaterThan(1000L);
        assertThat(Files.size(dark)).isGreaterThan(1000L);
        assertThat(Files.size(jetbrains)).isGreaterThan(1000L);

        renderPagesPng(light, outDir, "markdown-sample-light");
        renderPagesPng(dark, outDir, "markdown-sample-dark");
        renderPagesPng(jetbrains, outDir, "markdown-sample-jetbrains");
    }

    private static String load(String fixture) throws IOException {
        try (InputStream in = SamplePdfGenerationTest.class.getResourceAsStream("/markdown/" + fixture)) {
            assertThat(in).as("fixture %s on the classpath", fixture).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Renders every page of a PDF to a PNG (developer aid for eyeballing output). */
    private static void renderPagesPng(Path pdf, Path outDir, String stem) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pages = doc.getNumberOfPages();
            for (int i = 0; i < pages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 110);
                String suffix = pages == 1 ? "" : "-p" + (i + 1);
                ImageIO.write(image, "PNG", outDir.resolve(stem + suffix + ".png").toFile());
            }
        }
    }
}
