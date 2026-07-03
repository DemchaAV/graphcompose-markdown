package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A list item's continuation block (a second paragraph, a code block, …) must keep the item's
 * left indent, not render flush against the page margin — the regression this pins. It renders a
 * plain top-level paragraph followed by a loose list item whose second paragraph is a continuation,
 * then reads the glyph x-positions: the continuation must sit measurably further right than the
 * top-level paragraph (i.e. it is indented under the item, not hanging at the margin).
 */
class ListContinuationIndentTest {

    @Test
    void listItemContinuationParagraphKeepsTheItemIndent() throws IOException {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render("""
                MARGINWORD sits at the page margin.

                - ITEMWORD is the item's first paragraph

                  CONTINUATIONWORD is a second paragraph of the same item
                """).toPdfBytes();

        double marginX = firstGlyphX(pdf, "MARGINWORD");
        double continuationX = firstGlyphX(pdf, "CONTINUATIONWORD");

        assertThat(marginX).as("plain paragraph x").isNotNaN();
        assertThat(continuationX).as("continuation paragraph x").isNotNaN();
        // Before the fix the continuation rendered at the page margin (same x as the plain
        // paragraph, actually left of the item marker). It must now be indented under the item.
        assertThat(continuationX).as("continuation is indented past the page margin")
                .isGreaterThan(marginX + 8.0);
    }

    /** The x-coordinate of the first glyph of {@code needle} on page 1, or NaN if not found. */
    private static double firstGlyphX(byte[] pdf, String needle) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            double[] x = {Double.NaN};
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> positions) throws IOException {
                    int at = text.indexOf(needle);
                    if (Double.isNaN(x[0]) && at >= 0 && at < positions.size()) {
                        x[0] = positions.get(at).getXDirAdj();
                    }
                    super.writeString(text, positions);
                }
            };
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            stripper.getText(document);
            return x[0];
        }
    }
}
