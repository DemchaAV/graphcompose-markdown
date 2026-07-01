package io.github.demchaav.markdown.composer;

import io.github.demchaav.markdown.extension.ImageResolver;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MarkdownComposer#renderFile(Path)} reads a Markdown file and resolves relative image
 * paths against the file's own directory.
 */
class MarkdownComposerRenderFileTest {

    @Test
    void renderFileReadsContentAndResolvesRelativeImagesFromTheFileDirectory(@TempDir Path dir) throws Exception {
        // A real PNG sitting next to the document; the Markdown references it by a bare relative name.
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB), "png", dir.resolve("pic.png").toFile());

        Path md = dir.resolve("doc.md");
        Files.writeString(md, """
                # Title

                Body text here.

                ![DIAGRAMALT](pic.png)
                """, StandardCharsets.UTF_8);

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).renderFile(md).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("Title").contains("Body text here.");
            // The relative image resolved (rendered as an image), so its alt-text fallback is absent.
            // Without renderFile's directory wiring this would fall back to the alt text.
            assertThat(text).doesNotContain("DIAGRAMALT");
        }
    }

    @Test
    void renderStringDoesNotResolveRelativeImages_soRenderFileIsWhatMakesThemWork() throws Exception {
        // The contrast that proves renderFile's value: the same Markdown via render(String) has no
        // directory wiring, so the relative image can't resolve and its alt text is shown instead.
        String md = "# Title\n\n![DIAGRAMALT](pic.png)\n";
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(md).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(new PDFTextStripper().getText(doc)).contains("DIAGRAMALT");
        }
    }

    @Test
    void renderFileWithAnExplicitResolverUsesItInsteadOfTheFileDirectory(@TempDir Path dir) throws Exception {
        // The image is NOT next to the file; a custom resolver supplies it by name.
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB), "png", png);
        ImageResolver custom = source -> "logo.png".equals(source)
                ? Optional.of(png.toByteArray()) : Optional.empty();

        Path md = dir.resolve("doc.md");
        Files.writeString(md, "# Title\n\n![ALTX](logo.png)\n", StandardCharsets.UTF_8);

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).renderFile(md, custom).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            // Resolved by the custom resolver → rendered as an image, so the alt text is absent.
            assertThat(new PDFTextStripper().getText(doc)).doesNotContain("ALTX");
        }
    }

    @Test
    void renderFileRejectsNullAndReportsMissingFiles() {
        MarkdownComposer composer = MarkdownComposer.create(DefaultMarkdownTheme.light());
        assertThatThrownBy(() -> composer.renderFile(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> composer.renderFile(Path.of("no-such-file-8f3a1c.md")))
                .isInstanceOf(IOException.class);
    }

    @Test
    void renderFileWritesAPdfToDisk(@TempDir Path dir) throws Exception {
        Path md = dir.resolve("note.md");
        Files.writeString(md, "# Hello\n\nWorld.\n", StandardCharsets.UTF_8);
        Path out = dir.resolve("note.pdf");

        MarkdownComposer.create(DefaultMarkdownTheme.light()).renderFile(md).writePdf(out);

        assertThat(out).exists();
        assertThat(Files.size(out)).isGreaterThan(500L);
    }
}
