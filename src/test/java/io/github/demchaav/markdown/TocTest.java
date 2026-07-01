package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.TocNode;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A standalone {@code [TOC]} / {@code [[_TOC_]]} marker becomes an auto-generated, clickable table
 * of contents whose links jump to every heading (resolving to the same slugs the headings declare).
 */
class TocTest {

    private static MarkdownComposer composer() {
        return MarkdownComposer.create(DefaultMarkdownTheme.light());
    }

    @Test
    void tocMarkerMapsToATocNodeButOnlyWhenStandalone() {
        assertThat(composer().render("[TOC]").document().blocks())
                .singleElement().isInstanceOf(TocNode.class);
        assertThat(composer().render("[[_TOC_]]").document().blocks().get(0))
                .isInstanceOf(TocNode.class);
        assertThat(composer().render("[toc]").document().blocks().get(0))
                .isInstanceOf(TocNode.class);
        // Not a whole-line marker → ordinary paragraph, left as literal text.
        assertThat(composer().render("see [TOC] here").document().blocks().get(0))
                .isInstanceOf(ParagraphNode.class);
    }

    @Test
    void tocLinksJumpToEveryHeadingIncludingDeDuplicatedSlugs() throws Exception {
        String md = """
                [TOC]

                # Overview

                Body.

                # Overview

                More body.

                ## Details

                Even more.
                """;

        byte[] pdf = composer().render(md).toPdfBytes();

        // 3 headings → 3 TOC links. They resolve to overview, overview-1 (de-duplicated) and details
        // — the exact slugs the headings declare — so each is a native GoTo. A slug mismatch would
        // render that entry as plain text (no GoTo), so an exact count of 3 proves consistency.
        assertThat(countGoTo(pdf)).isEqualTo(3);
    }

    @Test
    void tocWithNoHeadingsRendersNothingAndDoesNotCrash() throws Exception {
        byte[] pdf = composer().render("[TOC]\n\nJust prose, no headings.").toPdfBytes();
        assertThat(countGoTo(pdf)).isZero();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    private static int countGoTo(byte[] pdf) throws Exception {
        int goTo = 0;
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            for (PDPage page : doc.getPages()) {
                for (PDAnnotation annotation : page.getAnnotations()) {
                    if (annotation instanceof PDAnnotationLink link && link.getAction() instanceof PDActionGoTo) {
                        goTo++;
                    }
                }
            }
        }
        return goTo;
    }
}
