package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.FrontMatterNode;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.ThematicBreakNode;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * YAML front matter ({@code ---} … {@code ---} at the top) parses into metadata and
 * renders a title block.
 */
class FrontMatterTest {

    private static MarkdownDocument model(String markdown) {
        return MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).document();
    }

    @Test
    void frontMatterParsesIntoMetadata() {
        MarkdownDocument doc = model("""
                ---
                title: My Report
                author: Jane Doe
                date: 2026-06-21
                ---

                # Body

                Some text.
                """);

        FrontMatterNode front = (FrontMatterNode) doc.blocks().get(0);
        assertThat(front.first("title")).isEqualTo("My Report");
        assertThat(front.first("author")).isEqualTo("Jane Doe");
        assertThat(front.first("date")).isEqualTo("2026-06-21");
    }

    @Test
    void frontMatterRendersATitleBlock() throws Exception {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render("""
                ---
                title: Quarterly Review
                subtitle: Q4 results
                author: Jane Doe
                date: 2026-06-21
                ---

                Body text follows the title block.
                """).toPdfBytes();

        String text = extractText(pdf);
        assertThat(text).contains("Quarterly Review");
        assertThat(text).contains("Q4 results");
        assertThat(text).contains("Jane Doe");
        assertThat(text).contains("Body text follows");
    }

    @Test
    void aDocumentWithoutFrontMatterHasNone() {
        MarkdownDocument doc = model("# Just a heading\n\nNo front matter here.");

        assertThat(doc.blocks()).noneMatch(FrontMatterNode.class::isInstance);
    }

    @Test
    void aMidDocumentRuleIsAThematicBreakNotFrontMatter() {
        MarkdownDocument doc = model("Para one.\n\n---\n\nPara two.");

        assertThat(doc.blocks()).noneMatch(FrontMatterNode.class::isInstance);
        assertThat(doc.blocks()).anyMatch(ThematicBreakNode.class::isInstance);
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }
}
