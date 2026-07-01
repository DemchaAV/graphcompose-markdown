package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

/**
 * Rasterizes Markdown straight to PNG page images — {@code Rendered.toImages(dpi)} — with no
 * PDF write / re-parse round-trip. Ideal for thumbnails, web preview cards, or CI snapshots.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.PngExportExample
 * </pre>
 */
public final class PngExportExample {

    private PngExportExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown = """
                # PNG export

                The same themed render that produces the PDF can rasterize
                **directly to images** — one `BufferedImage` per page.

                - `toImages(dpi)` — every page
                - `toImage(pageIndex, dpi)` — a single page (e.g. a thumbnail)
                """;

        List<BufferedImage> pages = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render(markdown)
                .toImages(150);

        for (int i = 0; i < pages.size(); i++) {
            Path out = Path.of("png-export-p" + (i + 1) + ".png");
            ImageIO.write(pages.get(i), "PNG", out.toFile());
            System.out.println("Wrote " + out.toAbsolutePath());
        }
    }
}
