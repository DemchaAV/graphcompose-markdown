package io.github.demchaav.markdown.composer;

import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MarkdownComposer.Rendered#toImages(int)} / {@link MarkdownComposer.Rendered#toImage(int, int)}
 * rasterize the document straight to page images, matching the PDF's page count — no PDF
 * write / re-parse round-trip.
 */
class MarkdownComposerToImagesTest {

    private static final String MULTI_PAGE = "# Report\n\n" + IntStream.range(0, 200)
            .mapToObj(i -> "Paragraph " + i + " — filler text to span multiple pages.")
            .collect(Collectors.joining("\n\n"));

    @Test
    void toImagesYieldsOneImagePerPdfPage() throws Exception {
        MarkdownComposer composer = MarkdownComposer.create(DefaultMarkdownTheme.light());

        List<BufferedImage> images = composer.render(MULTI_PAGE).toImages(96);

        int pdfPages;
        try (PDDocument doc = Loader.loadPDF(composer.render(MULTI_PAGE).toPdfBytes())) {
            pdfPages = doc.getNumberOfPages();
        }
        assertThat(pdfPages).isGreaterThan(1);
        assertThat(images).hasSize(pdfPages);
        assertThat(images).allSatisfy(image -> {
            assertThat(image.getWidth()).isGreaterThan(0);
            assertThat(image.getHeight()).isGreaterThan(0);
        });
    }

    @Test
    void toImageRendersASinglePage() throws Exception {
        BufferedImage second = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render(MULTI_PAGE).toImage(1, 96);

        assertThat(second.getWidth()).isGreaterThan(0);
        assertThat(second.getHeight()).isGreaterThan(0);
    }

    @Test
    void invalidDpiAndNegativePageIndexAreRejected() {
        MarkdownComposer.Rendered rendered = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("# One page\n\nShort.");

        assertThatThrownBy(() -> rendered.toImages(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rendered.toImage(-1, 96)).isInstanceOf(IndexOutOfBoundsException.class);
        // Beyond the last page exercises the post-layout backend path, not the cheap precondition.
        assertThatThrownBy(() -> rendered.toImage(99, 96)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void singlePageDocumentYieldsExactlyOneImage() throws Exception {
        List<BufferedImage> images = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("# One page\n\nShort.").toImages(96);

        assertThat(images).hasSize(1);
    }
}
