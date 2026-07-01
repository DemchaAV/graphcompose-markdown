package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end proof of v1.9 in-PDF navigation: {@code [text](#heading)} links and
 * footnote references become native PDF {@code GoTo} actions, while external links
 * stay {@code URI} actions.
 */
class InPdfNavigationRenderTest {

    @Test
    void internalHeadingLinkEmitsGoToWhileExternalStaysUri() throws Exception {
        String md = """
                [Jump to target](#the-target) and an [external](https://example.com) link.

                ## The Target

                Body text under the target heading.
                """;

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(md).toPdfBytes();

        int[] counts = countLinkActions(pdf);
        assertThat(counts[0]).as("internal #link resolves to a GoTo action").isGreaterThanOrEqualTo(1);
        assertThat(counts[1]).as("external link stays a URI action").isGreaterThanOrEqualTo(1);
    }

    @Test
    void footnoteReferenceAndNoteAreBidirectional() throws Exception {
        String md = """
                A claim that needs a source.[^1]

                [^1]: The supporting note.
                """;

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(md).toPdfBytes();

        int[] counts = countLinkActions(pdf);
        // ref -> note (fn-1) and the note's marker -> ref (fnref-1): two in-document jumps.
        assertThat(counts[0]).as("a bidirectional footnote yields two GoTo actions")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void footnoteCitedFromAListItemIsAlsoBidirectional() throws Exception {
        // List items render their leading paragraph inline (bypassing ParagraphRenderer), so the
        // back-anchor must be placed there too — otherwise this is 1 GoTo (forward only), not 2.
        String md = """
                - A list item with a citation.[^1]

                [^1]: The supporting note.
                """;

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(md).toPdfBytes();

        assertThat(countLinkActions(pdf)[0])
                .as("a footnote cited from a list item is bidirectional (2 GoTo)")
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void footnoteCitedFromATableCellIsAlsoBidirectional() throws Exception {
        // Table cell paragraphs bypass ParagraphRenderer, so the back-anchor must be placed
        // there too — otherwise this is 1 GoTo (forward only), not 2.
        String md = """
                | Claim | Source |
                |---|---|
                | Water is wet.[^1] | lab notes |

                [^1]: The supporting note.
                """;

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(md).toPdfBytes();

        assertThat(countLinkActions(pdf)[0])
                .as("a footnote cited from a table cell is bidirectional (2 GoTo)")
                .isGreaterThanOrEqualTo(2);
    }

    /** @return {@code [goToCount, uriCount]} over every link annotation in the PDF */
    private static int[] countLinkActions(byte[] pdf) throws Exception {
        int goTo = 0;
        int uri = 0;
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            for (PDPage page : doc.getPages()) {
                for (PDAnnotation annotation : page.getAnnotations()) {
                    if (annotation instanceof PDAnnotationLink link) {
                        PDAction action = link.getAction();
                        if (action instanceof PDActionGoTo) {
                            goTo++;
                        } else if (action instanceof PDActionURI) {
                            uri++;
                        }
                    }
                }
            }
        }
        return new int[]{goTo, uri};
    }
}
