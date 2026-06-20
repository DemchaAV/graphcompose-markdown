package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Headings become a navigable PDF outline (bookmark tree), nested by heading level.
 */
class HeadingBookmarkTest {

    @Test
    void headingsBecomeANestedPdfOutline() throws Exception {
        String md = """
                # Top

                Body text.

                ## Sub A

                More body.

                ## Sub B

                ### Deep
                """;

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(md).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
            assertThat(outline).as("the PDF has a document outline").isNotNull();

            // Depth-first the whole tree: every heading is present, in order.
            List<String> titles = new ArrayList<>();
            collectTitles(outline, titles);
            assertThat(titles).containsExactly("Top", "Sub A", "Sub B", "Deep");

            // Nesting: h1 is the single root; the two h2s nest under it; the h3 under "Sub B".
            assertThat(outline.getFirstChild().getTitle()).isEqualTo("Top");
            List<String> topChildren = new ArrayList<>();
            for (PDOutlineItem child : outline.getFirstChild().children()) {
                topChildren.add(child.getTitle());
            }
            assertThat(topChildren).containsExactly("Sub A", "Sub B");
        }
    }

    @Test
    void bookmarkTitleIsThePlainHeadingText() throws Exception {
        // Inline formatting in the heading must not leak markup into the outline title.
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("# A **bold** `title` link").toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
            assertThat(outline.getFirstChild().getTitle()).isEqualTo("A bold title link");
        }
    }

    private static void collectTitles(PDOutlineNode node, List<String> titles) {
        for (PDOutlineItem item : node.children()) {
            titles.add(item.getTitle());
            collectTitles(item, titles);
        }
    }
}
