package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.extension.DefaultImageResolver;
import io.github.demchaav.markdown.model.TocNode;
import io.github.demchaav.markdown.render.BookTocRenderer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.FooterTokens;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the committed user manual ({@code assets/readme/manual.md}) with the exact composer
 * its final section documents, and asserts the features it demonstrates actually land in the
 * output. The committed {@code manual.pdf} is produced from the same source and composer; this
 * test keeps the manual honest.
 */
class ManualTest {

    private static final Path BASE = Path.of("assets", "readme");

    /** The composer the manual's "How this PDF was made" section shows — kept in lockstep. */
    static MarkdownComposer manualComposer() {
        MarkdownTheme base = DefaultMarkdownTheme.light();
        MarkdownTheme manualTheme = MarkdownTheme.builder(base)
                .renderer(TocNode.class, new BookTocRenderer())
                .tokens(base.tokens().withFooter(FooterTokens.pageNumbers()))
                .imageResolver(new DefaultImageResolver(BASE))
                .build();
        return MarkdownComposer.builder()
                .theme(manualTheme)
                .smartPunctuation(true)
                .build();
    }

    @Test
    void theManualRendersItsWholeFeatureSet() throws Exception {
        byte[] pdf = manualComposer().renderFile(BASE.resolve("manual.md")).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            int pages = doc.getNumberOfPages();
            assertThat(pages).as("a real manual spans pages").isGreaterThan(2);

            // Chrome: the outline panel opens; the footer numbers every page.
            assertThat(doc.getDocumentCatalog().getPageMode()).isEqualTo(PageMode.USE_OUTLINES);
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("Page 1 of " + pages).contains("Page 2 of " + pages);

            // Front matter title block and the major sections.
            assertThat(text).contains("GraphCompose Markdown")
                    .contains("User Manual")
                    .contains("What is this?")
                    .contains("How this PDF was made");

            // Smart punctuation was applied (em-dash present, raw '---' gone from prose)…
            assertThat(text).contains("—").contains("“");
            // …while code stayed verbatim.
            assertThat(text).contains("--flag");

            // Emoji: unknown shortcode degrades to readable text.
            assertThat(text).contains(":definitelynotanemoji:");

            // Footnote section rendered.
            assertThat(text).contains("Notes");
        }
    }

    @Test
    void theManualContentsResolvesPageNumbers() throws Exception {
        byte[] pdf = manualComposer().renderFile(BASE.resolve("manual.md")).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            String tocPage = stripper.getText(doc);
            // The book TOC on page 1 lists later sections with resolved page numbers.
            assertThat(tocPage).contains("Theming").contains("Command line");
            assertThat(tocPage).containsPattern("[2-9]");
        }
    }
}
