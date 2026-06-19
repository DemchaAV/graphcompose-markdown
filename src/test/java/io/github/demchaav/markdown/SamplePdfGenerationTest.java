package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders a showcase document with the light and dark themes into
 * {@code target/generated-pdfs/} so the output can be eyeballed. Doubles as an
 * end-to-end smoke test that the showcase content renders.
 */
class SamplePdfGenerationTest {

    private static final String SAMPLE = """
            # GraphCompose Markdown

            A themeable Markdown document composer powered by the **GraphCompose**
            layout engine. The same Markdown can be reskinned without touching its
            text — *content* and *appearance* stay separate.

            ## Inline formatting

            Paragraphs support **bold**, *italic*, ***bold italic***,
            ~~strikethrough~~, `inline code`, and [links](https://github.com/DemchaAV/GraphCompose).

            ## Lists

            - Headings, paragraphs and lists
            - Nested items:
                - second level
                - second level again
            - Back to the first level

            1. Ordered items
            2. Keep their numbers
            3. Across the list

            ## Code

            ```java
            MarkdownComposer composer = MarkdownComposer.builder()
                    .theme(DefaultMarkdownTheme.light())
                    .build();
            composer.render(markdown).writePdf(Path.of("out.pdf"));
            ```

            ## Quotes and rules

            > Themes decide how all of this looks — design tokens for the cosmetics,
            > node renderers for the behaviour.

            ---

            :::callout warning
            Custom `:::` blocks render through a registered renderer. Swap the
            renderer to restyle every callout at once.
            :::

            :::callout tip
            Derive a new theme from an existing one and override only what differs.
            :::
            """;

    @Test
    void writesLightAndDarkSamplePdfs() throws Exception {
        Path outDir = Path.of("target", "generated-pdfs");
        Files.createDirectories(outDir);

        Path light = outDir.resolve("markdown-sample-light.pdf");
        Path dark = outDir.resolve("markdown-sample-dark.pdf");

        MarkdownComposer.create(DefaultMarkdownTheme.light()).render(SAMPLE).writePdf(light);
        MarkdownComposer.create(DefaultMarkdownTheme.dark()).render(SAMPLE).writePdf(dark);

        assertThat(light).exists();
        assertThat(dark).exists();
        assertThat(Files.size(light)).isGreaterThan(1000L);
        assertThat(Files.size(dark)).isGreaterThan(1000L);

        renderFirstPagePng(light, outDir.resolve("markdown-sample-light.png"));
        renderFirstPagePng(dark, outDir.resolve("markdown-sample-dark.png"));
    }

    /** Renders page 1 of a PDF to a PNG (developer aid for eyeballing output). */
    private static void renderFirstPagePng(Path pdf, Path png) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage image = renderer.renderImageWithDPI(0, 110);
            ImageIO.write(image, "PNG", png.toFile());
        }
    }
}
