package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.TocNode;
import io.github.demchaav.markdown.render.BookTocRenderer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BookTocRenderer} (opted in by swapping the {@code TocNode} renderer) renders a
 * page-numbered, dot-leader table of contents whose page numbers are resolved from the
 * laid-out document, with clickable entries.
 */
class BookTocTest {

    /** Digit-free filler so a page number in the TOC is unambiguous in extracted text. */
    private static final String FILLER = IntStream.range(0, 40)
            .mapToObj(i -> "Filler prose to push the following chapter onto a later page.")
            .collect(Collectors.joining("\n\n"));

    private static MarkdownComposer bookComposer(String title) {
        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .renderer(TocNode.class, new BookTocRenderer(title))
                .build();
        return MarkdownComposer.create(theme);
    }

    @Test
    void bookTocResolvesPageNumbersAndStaysClickable() throws Exception {
        String md = "[TOC]\n\n# Alpha\n\n" + FILLER + "\n\n# Omega\n\nShort tail.\n";

        byte[] pdf = bookComposer("Contents").render(md).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(1);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            String tocPage = stripper.getText(doc);
            // Title row + both entries on the contents page.
            assertThat(tocPage).contains("Contents").contains("Alpha").contains("Omega");
            // "Omega" starts on a later page, and that resolved number is printed in the TOC.
            // The filler is digit-free, so any digit >= 2 on page 1 is a TOC page number.
            assertThat(tocPage).containsPattern("[2-9]");
            // Entries remain clickable in-document jumps.
            assertThat(countGoTo(doc)).isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void bookTocWithoutHeadingsRendersNothingAndDoesNotCrash() throws Exception {
        byte[] pdf = bookComposer(null).render("[TOC]\n\nJust prose, no headings.").toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(countGoTo(doc)).isZero();
        }
    }

    private static int countGoTo(PDDocument doc) throws Exception {
        int goTo = 0;
        for (PDPage page : doc.getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation instanceof PDAnnotationLink link && link.getAction() instanceof PDActionGoTo) {
                    goTo++;
                }
            }
        }
        return goTo;
    }
}
