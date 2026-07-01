package io.github.demchaav.markdown.composer;

import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PageMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A rendered PDF with headings asks the viewer to open its bookmark/outline panel
 * ({@code PageMode.USE_OUTLINES}); the preference is skipped for heading-less documents and can
 * be turned off with {@link MarkdownComposer.Builder#openOutline(boolean)}.
 */
class MarkdownComposerViewerPreferencesTest {

    private static final String WITH_HEADINGS = """
            # Top

            Body text.

            ## Sub

            More body.
            """;

    @Test
    void documentWithHeadingsOpensTheOutlinePanel() throws Exception {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(WITH_HEADINGS).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(doc.getDocumentCatalog().getPageMode()).isEqualTo(PageMode.USE_OUTLINES);
        }
    }

    @Test
    void openOutlineFalseLeavesThePageModeAlone() throws Exception {
        byte[] pdf = MarkdownComposer.builder().openOutline(false).build()
                .render(WITH_HEADINGS).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(doc.getDocumentCatalog().getPageMode()).isNotEqualTo(PageMode.USE_OUTLINES);
        }
    }

    @Test
    void headingLessDocumentDoesNotOpenAnEmptyOutlinePanel() throws Exception {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("Just a paragraph, no headings at all.").toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(doc.getDocumentCatalog().getPageMode()).isNotEqualTo(PageMode.USE_OUTLINES);
        }
    }
}
