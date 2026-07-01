package io.github.demchaav.markdown.composer;

import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.FooterTokens;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The optional running footer ({@link FooterTokens}) draws {@code {page}}/{@code {pages}} page
 * numbers when enabled, is absent by default, and honours {@code showOnFirstPage}.
 */
class MarkdownComposerFooterTest {

    /** Markdown long enough to paginate across several pages. */
    private static final String LONG_MARKDOWN = "# Report\n\n" + IntStream.range(0, 220)
            .mapToObj(i -> "Paragraph " + i + " — filler text to push the document across page boundaries.")
            .collect(Collectors.joining("\n\n"));

    @Test
    void enabledFooterDrawsPageNumbersOnEveryPage() throws Exception {
        MarkdownTheme theme = withFooter(FooterTokens.pageNumbers());

        byte[] pdf = MarkdownComposer.create(theme).render(LONG_MARKDOWN).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            int pages = doc.getNumberOfPages();
            assertThat(pages).as("test doc spans multiple pages").isGreaterThan(1);
            assertThat(fullText(doc)).contains("Page 1 of " + pages).contains("Page 2 of " + pages);
        }
    }

    @Test
    void defaultThemeHasNoFooter() throws Exception {
        assertThat(DefaultMarkdownTheme.light().tokens().footer().enabled()).isFalse();

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(LONG_MARKDOWN).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(fullText(doc)).doesNotContain("Page 1 of");
        }
    }

    @Test
    void showOnFirstPageFalseSuppressesTheFooterOnPageOne() throws Exception {
        MarkdownTheme theme = withFooter(FooterTokens.pageNumbers().withShowOnFirstPage(false));

        byte[] pdf = MarkdownComposer.create(theme).render(LONG_MARKDOWN).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(1);
            assertThat(pageText(doc, 1)).as("page 1 footer suppressed").doesNotContain("Page 1 of");
            assertThat(pageText(doc, 2)).as("page 2 keeps its footer").contains("Page 2 of");
        }
    }

    @Test
    void customLeftCenterRightFooterTextAllRender() throws Exception {
        // The general (non-preset) path: all three positions + a custom {page} template.
        FooterTokens custom = new FooterTokens(true, "CONFIDENTIAL", "Page {page}", "Rev A",
                9.0, DocumentColor.rgb(120, 120, 120), true);

        byte[] pdf = MarkdownComposer.create(withFooter(custom)).render(LONG_MARKDOWN).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(fullText(doc)).contains("CONFIDENTIAL").contains("Page 1").contains("Rev A");
        }
    }

    private static MarkdownTheme withFooter(FooterTokens footer) {
        MarkdownTheme base = DefaultMarkdownTheme.light();
        return MarkdownTheme.builder(base).tokens(base.tokens().withFooter(footer)).build();
    }

    private static String fullText(PDDocument doc) throws Exception {
        return new PDFTextStripper().getText(doc);
    }

    private static String pageText(PDDocument doc, int page) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        return stripper.getText(doc);
    }
}
