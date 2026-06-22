package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Edge-case tables and lists that the fixture suite did not cover: a ragged GFM table (a body
 * row with fewer cells than the header), a table with an empty cell, and a deeply (3-level)
 * nested list. Each must render without crashing and keep every cell / item's text.
 */
class TableAndListEdgeCasesTest {

    private static byte[] render(String markdown) {
        return MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).toPdfBytes();
    }

    private static String text(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    @Test
    void raggedTableRendersAndKeepsEveryCellsText() throws IOException {
        // The middle row has two cells against a three-column header.
        byte[] pdf = render("""
                | A | B | C |
                |---|---|---|
                | 1 | 2 |
                | x | y | z |
                """);

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(text(pdf)).contains("A").contains("B").contains("C")
                .contains("1").contains("2").contains("x").contains("y").contains("z");
    }

    @Test
    void tableWithAnEmptyCellRenders() throws IOException {
        byte[] pdf = render("""
                | Key | Value |
                |-----|-------|
                |     | set   |
                | k   |       |
                """);

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(text(pdf)).contains("Key").contains("Value").contains("set").contains("k");
    }

    @Test
    void threeLevelNestedListRendersAndKeepsEveryItemsText() throws IOException {
        byte[] pdf = render("""
                - one
                    - two
                        - three
                """);

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(text(pdf)).contains("one").contains("two").contains("three");
    }
}
