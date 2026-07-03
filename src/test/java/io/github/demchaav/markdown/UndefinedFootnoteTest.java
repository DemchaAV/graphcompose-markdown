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
 * A footnote reference with no matching definition ({@code [^missing]}) must render as its literal
 * source — the CommonMark behaviour — not consume a number and leave a dangling superscript with a
 * gap in the run. A defined footnote alongside it still numbers and renders normally.
 */
class UndefinedFootnoteTest {

    private static String text(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    @Test
    void undefinedFootnoteRendersAsLiteralTextAndDefinedOneStillWorks() throws IOException {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render("""
                A real note[^a] next to a dangling one[^missing].

                [^a]: The defined note body.
                """).toPdfBytes();

        String text = text(pdf);
        // The defined footnote's body is rendered in the Notes section.
        assertThat(text).contains("The defined note body");
        // The undefined reference survives as its literal source rather than a numbered superscript.
        assertThat(text).contains("[^missing]");
    }
}
