package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;
import io.github.demchaav.markdown.theme.tokens.PageTokens;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * @return the 1-based page carrying {@code needle}
     * @throws AssertionError when no page does — otherwise a broken render would let
     *                        two "not found" results compare equal and pass silently
     */
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
            throw new AssertionError("'" + needle + "' is on no page of the rendered document");
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
     * The outline entry points at the page the heading landed on after relocating,
     * rather than the one it left. The wrapper carries the whole heading and moves as
     * a unit, so this holds whichever node the bookmark rides — what it guards is the
     * relocation itself staying in step with the outline, not the choice of node.
     * (That choice is visible only in the destination's Y: a bookmark on the wrapper
     * would point at the top of the heading's space-above margin instead of at the
     * text, ~8 pt higher for an H2 in the default theme.)
     */
    @Test
    void theBookmarkFollowsTheRelocatedHeading() throws Exception {
        byte[] pdf = render(markdown(ORPHANING_FILLER), true);
        int headingPage = pageOf(pdf, HEADING);
        assertThat(headingPage).as("the sample really does relocate the heading").isEqualTo(2);

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDOutlineItem bookmark = doc.getDocumentCatalog().getDocumentOutline().getFirstChild();
            while (bookmark != null && !HEADING.equals(bookmark.getTitle())) {
                bookmark = bookmark.getFirstChild() != null && HEADING.equals(bookmark.getFirstChild().getTitle())
                        ? bookmark.getFirstChild()
                        : bookmark.getNextSibling();
            }
            assertThat(bookmark).as("the heading has an outline entry").isNotNull();
            assertThat(destinationPage(doc, bookmark))
                    .as("the outline entry points at the page the heading landed on")
                    .isEqualTo(headingPage);
        }
    }

    /**
     * A {@code [text](#slug)} link to the relocated heading resolves to the page it
     * landed on. The anchor rides the same paragraph as the bookmark, but it travels
     * as a named destination rather than an outline entry, so it is a distinct path.
     */
    @Test
    void anAnchorLinkFollowsTheRelocatedHeading() throws Exception {
        String md = "[jump](#target-heading)\n\n" + markdown(ORPHANING_FILLER);
        byte[] pdf = render(md, true);
        int headingPage = pageOf(pdf, HEADING);

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            List<Integer> targets = new ArrayList<>();
            for (PDPage page : doc.getPages()) {
                for (PDAnnotation annotation : page.getAnnotations()) {
                    if (annotation instanceof PDAnnotationLink link) {
                        PDDestination destination = link.getDestination();
                        if (destination == null && link.getAction() instanceof PDActionGoTo goTo) {
                            destination = goTo.getDestination();
                        }
                        if (destination != null) {
                            targets.add(resolvePage(doc, destination));
                        }
                    }
                }
            }
            assertThat(targets).as("the anchor link resolves to the heading's page")
                    .containsExactly(headingPage);
        }
    }

    /** @return the 1-based page an outline item points at */
    private static int destinationPage(PDDocument doc, PDOutlineItem item) throws Exception {
        PDDestination destination = item.getDestination();
        if (destination == null && item.getAction() instanceof PDActionGoTo goTo) {
            destination = goTo.getDestination();
        }
        return resolvePage(doc, destination);
    }

    /** @return the 1-based page a destination resolves to, or -1 when it cannot be resolved */
    private static int resolvePage(PDDocument doc, PDDestination destination) throws Exception {
        PDDestination resolved = destination;
        if (resolved instanceof PDNamedDestination named) {
            resolved = doc.getDocumentCatalog().findNamedDestinationPage(named);
        }
        if (resolved instanceof PDPageDestination pageDestination) {
            int number = pageDestination.retrievePageNumber();
            return number >= 0
                    ? number + 1
                    : doc.getPages().indexOf(pageDestination.getPage()) + 1;
        }
        return -1;
    }
}
