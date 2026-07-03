package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tables extension is pinned to GitHub-Flavored-Markdown semantics. Flexmark's defaults
 * accept a trailing {@code [caption]} line (and column spans, and extra header rows) that the
 * mapper does not model and would silently drop; under GFM parity that line is just ordinary
 * text and survives. This pins the caption case, which is the one that swallowed user content.
 */
class TableGfmParityTest {

    private static String text(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static byte[] render(String markdown) {
        return MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).toPdfBytes();
    }

    @Test
    void aTrailingCaptionLineIsKeptAsTextNotSwallowed() throws IOException {
        // With flexmark's default WITH_CAPTION, the "[CAPTIONWORD]" line is parsed as a table
        // caption the mapper drops. Under GFM parity it is plain text and must reach the page.
        byte[] pdf = render("""
                | A | B |
                |---|---|
                | 1 | 2 |
                [CAPTIONWORD after the table]
                """);

        String text = text(pdf);
        assertThat(text).contains("A").contains("B").contains("1").contains("2");
        assertThat(text).as("the trailing caption line is not silently swallowed")
                .contains("CAPTIONWORD");
    }

    @Test
    void anOrdinaryTableStillRenders() throws IOException {
        byte[] pdf = render("""
                | Feature | Status |
                |---------|--------|
                | Tables  | Done   |
                """);

        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(text(pdf)).contains("Feature").contains("Status").contains("Tables").contains("Done");
    }
}
