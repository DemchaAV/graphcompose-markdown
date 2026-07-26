package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;
import io.github.demchaav.markdown.theme.tokens.PageTokens;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A heading is never left stranded as the last block on a page, apart from the
 * content it introduces. The renderer wraps the heading paragraph in a one-child
 * section marked {@code keepWithNext()} — the engine's opt-in orphan rule — so a
 * heading that cannot share its page with the first line of the following block
 * moves down to join it.
 *
 * <p>The filler count is tuned so the heading lands exactly in the window where
 * the heading itself still fits but its body's first line does not: that is the
 * only situation the rule acts on, and the {@code false} case pins the untreated
 * behaviour so the test would fail if the wrapping silently stopped applying.</p>
 */
class HeadingKeepWithNextTest {

    /** The filler count that puts the heading at the bottom of page 1 with no room for its body. */
    private static final int ORPHANING_FILLER = 32;

    private static final String HEADING = "Target heading";
    private static final String BODY_OPENING = "Body sentence that belongs";

    private static String markdown(int fillerParagraphs) {
        StringBuilder sb = new StringBuilder("# Doc\n\n");
        for (int i = 0; i < fillerParagraphs; i++) {
            sb.append("Filler paragraph number ").append(i)
                    .append(" with enough words to occupy a full line of the page body text.\n\n");
        }
        return sb.append("## ").append(HEADING).append("\n\n")
                .append(BODY_OPENING).append(" under the target heading and must not be torn from it.\n\n")
                .append("More body so the section is not trivially short and keeps flowing after the break.\n")
                .toString();
    }

    private static MarkdownTheme theme(boolean keepHeadingWithNext) {
        MarkdownTheme base = DefaultMarkdownTheme.light();
        MarkdownTokens tokens = base.tokens();
        PageTokens page = tokens.page();
        return MarkdownTheme.builder(base)
                .tokens(tokens.withPage(new PageTokens(
                        page.pageSize(), page.margin(), page.contentWidth(), keepHeadingWithNext)))
                .build();
    }

    /** @return the 1-based page carrying {@code needle}, or -1 when no page does */
    private static int pageOf(byte[] pdf, String needle) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            for (int page = 1; page <= doc.getNumberOfPages(); page++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                if (stripper.getText(doc).contains(needle)) {
                    return page;
                }
            }
            return -1;
        }
    }

    private static byte[] render(String markdown, boolean keepHeadingWithNext) {
        return MarkdownComposer.builder()
                .theme(theme(keepHeadingWithNext))
                .build()
                .render(markdown)
                .toPdfBytes();
    }

    @Test
    void defaultThemeKeepsHeadingsWithTheirContent() {
        assertThat(DefaultMarkdownTheme.light().tokens().page().keepHeadingWithNext()).isTrue();
        assertThat(DefaultMarkdownTheme.dark().tokens().page().keepHeadingWithNext()).isTrue();
    }

    @Test
    void headingMovesDownRatherThanStrandingAtThePageBottom() throws Exception {
        byte[] pdf = render(markdown(ORPHANING_FILLER), true);

        assertThat(pageOf(pdf, HEADING))
                .as("the heading shares a page with the body it introduces")
                .isEqualTo(pageOf(pdf, BODY_OPENING));
    }

    @Test
    void withoutTheTokenTheHeadingStrandsAtThePageBottom() throws Exception {
        byte[] pdf = render(markdown(ORPHANING_FILLER), false);

        assertThat(pageOf(pdf, HEADING))
                .as("the untreated flow leaves the heading behind on the previous page")
                .isLessThan(pageOf(pdf, BODY_OPENING));
    }

    /**
     * The rule only acts in the narrow window where the heading strands; a heading
     * with room for its body is placed exactly as before, on both settings.
     */
    @Test
    void headingWithRoomForItsBodyIsUnaffected() throws Exception {
        String md = markdown(10);

        assertThat(pageOf(render(md, true), HEADING)).isEqualTo(pageOf(render(md, false), HEADING));
    }

    /**
     * The wrapping section carries pagination only — the bookmark and the anchor
     * stay on the heading paragraph, so the outline is unchanged.
     */
    @Test
    void wrappingDoesNotDisturbTheOutline() throws Exception {
        byte[] pdf = render(markdown(ORPHANING_FILLER), true);

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(doc.getDocumentCatalog().getDocumentOutline()).isNotNull();
            assertThat(doc.getDocumentCatalog().getDocumentOutline().getFirstChild().getTitle())
                    .isEqualTo("Doc");
        }
    }
}
