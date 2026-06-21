package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.extension.DefaultImageResolver;
import io.github.demchaav.markdown.extension.EmojiResolver;
import io.github.demchaav.markdown.model.FrontMatterNode;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders the committed showcase document ({@code assets/readme/showcase.md}) — the page
 * that exercises every feature and doubles as the library's own documentation — and
 * asserts the whole feature set lands in the output. The committed
 * {@code showcase.pdf} / {@code showcase-p*.png} were produced from the same source and
 * theme; this test keeps the document honest.
 */
class ShowcaseTest {

    private static final Path BASE = Path.of("assets", "readme");

    /** The theme used to render the committed showcase: image + Twemoji emoji from assets/readme. */
    static MarkdownTheme showcaseTheme() {
        EmojiResolver emoji = shortcode -> {
            Path png = BASE.resolve("emoji").resolve(shortcode + ".png");
            try {
                return Files.exists(png) ? Optional.of(Files.readAllBytes(png)) : Optional.empty();
            } catch (IOException e) {
                return Optional.empty();
            }
        };
        return MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .imageResolver(new DefaultImageResolver(BASE))
                .emojiResolver(emoji)
                .build();
    }

    @Test
    void theShowcaseRendersEveryFeatureToPdf() throws Exception {
        String markdown = Files.readString(BASE.resolve("showcase.md"), StandardCharsets.UTF_8);

        MarkdownComposer.Rendered rendered = MarkdownComposer.create(showcaseTheme()).render(markdown);
        byte[] pdf = rendered.toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
        // front matter is the first block (and renders the title)
        assertThat(rendered.document().blocks().get(0)).isInstanceOf(FrontMatterNode.class);

        String text = extractText(pdf);
        assertThat(text).contains("GraphCompose Markdown"); // front-matter title block
        assertThat(text).contains("Note");                  // an alert title
        assertThat(text).contains("Caution");               // the last alert
        assertThat(text).contains("Headings to outline");   // a table cell
        assertThat(text).contains("Level six");             // h6
        assertThat(text).contains("Notes");                 // the footnotes section
    }

    private static String header(byte[] pdf) {
        return new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.US_ASCII);
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }
}
