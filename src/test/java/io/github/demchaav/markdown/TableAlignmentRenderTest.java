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
 * Column alignment is verified at the model level by the mapper test, but nothing proved it
 * reaches the page. This renders the same word in a wide column once left-aligned and once
 * right-aligned and reads the glyph x-positions back out of the PDF: the right-aligned word
 * must sit measurably further right.
 */
class TableAlignmentRenderTest {

    // A header wide enough that a short cell value has room to move within the column.
    private static final String WIDE_HEADER = "HEADER WIDE ENOUGH TO LEAVE ROOM";
    private static final String CELL = "WIDGET";

    @Test
    void rightAlignedCellRendersFurtherRightThanLeftAligned() throws IOException {
        byte[] leftAligned = render("| " + WIDE_HEADER + " |\n|:--|\n| " + CELL + " |");
        byte[] rightAligned = render("| " + WIDE_HEADER + " |\n|--:|\n| " + CELL + " |");

        double leftX = firstGlyphX(leftAligned, CELL);
        double rightX = firstGlyphX(rightAligned, CELL);

        assertThat(leftX).as("left-aligned cell x").isNotNaN();
        assertThat(rightX).as("right-aligned cell x").isNotNaN();
        assertThat(rightX).as("right-aligned cell sits further right than left-aligned")
                .isGreaterThan(leftX + 50.0);
    }

    private static byte[] render(String markdown) {
        return MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).toPdfBytes();
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
