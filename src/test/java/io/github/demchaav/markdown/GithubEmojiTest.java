package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.extension.ClasspathEmojiResolver;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.inline.EmojiRun;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Emoji shortcodes ({@code :rocket:}) parse to an {@link EmojiRun} and resolve in priority
 * order: an inline image from a configured {@code EmojiResolver}, else a vector colour glyph
 * from the optional {@code graph-compose-emoji} set (on this test classpath), else readable
 * {@code :shortcode:} text — never a broken glyph.
 */
class GithubEmojiTest {

    private static MarkdownDocument model(String markdown) {
        return MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).document();
    }

    @Test
    void aKnownShortcodeParsesToAnEmojiRun() {
        MarkdownDocument doc = model("Launch :rocket: now");

        ParagraphNode paragraph = (ParagraphNode) doc.blocks().get(0);
        assertThat(paragraph.content())
                .anyMatch(n -> n instanceof EmojiRun emoji && emoji.shortcode().equals("rocket"));
    }

    @Test
    void anUnresolvedShortcodeRendersAsItsLiteralText() throws Exception {
        // flexmark turns any :word: into an emoji node; an emoji with no image (unknown, or
        // no resolver) falls back to its literal :shortcode: text — never a broken glyph.
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("see :definitelynotanemoji: here").toPdfBytes();

        assertThat(extractText(pdf)).contains(":definitelynotanemoji:");
    }

    @Test
    void withoutAResolverAKnownShortcodeRendersAsAVectorGlyph() throws Exception {
        // graph-compose-emoji is on the test classpath, so with no resolver a known shortcode
        // becomes a vector colour glyph: the surrounding text survives, the literal :rocket:
        // does not appear (it is drawn as vector art, not text).
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("Launch :rocket: now").toPdfBytes();

        String text = extractText(pdf);
        assertThat(text).contains("Launch").contains("now");
        assertThat(text).doesNotContain(":rocket:");
    }

    @Test
    void withAResolverEmojiRendersAsAnInlineImage() throws Exception {
        byte[] png = tinyPng();
        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .emojiResolver(shortcode -> Optional.of(png))
                .build();

        byte[] pdf = MarkdownComposer.create(theme).render("Launch :rocket: now").toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
        // the shortcode text is replaced by an inline image, so it is gone from the text layer
        assertThat(extractText(pdf)).doesNotContain(":rocket:");
    }

    @Test
    void classpathResolverFindsBundledEmojiAndMissesOthers() {
        // /emoji/rocket.png exists in test resources; :smile: does not.
        ClasspathEmojiResolver resolver = new ClasspathEmojiResolver("emoji");

        assertThat(resolver.resolve("rocket")).isPresent();
        assertThat(resolver.resolve("smile")).isEmpty();
    }

    @Test
    void classpathResolverRejectsPathTraversalShortcodes() {
        // Untrusted Markdown must not be able to walk the classpath via the resource path.
        ClasspathEmojiResolver resolver = new ClasspathEmojiResolver("emoji");

        assertThat(resolver.resolve("../../rocket")).isEmpty();
        assertThat(resolver.resolve("../rocket")).isEmpty();
        assertThat(resolver.resolve("a/b")).isEmpty();
        assertThat(resolver.resolve("rocket")).isPresent(); // the legitimate one still resolves
    }

    @Test
    void classpathResolvedEmojiRendersAsAnInlineImage() throws Exception {
        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .emojiResolver(new ClasspathEmojiResolver("emoji"))
                .build();

        byte[] pdf = MarkdownComposer.create(theme).render("Launch :rocket: now").toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
        assertThat(extractText(pdf)).doesNotContain(":rocket:");
    }

    private static byte[] tinyPng() throws IOException {
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                img.setRGB(x, y, 0xFFFF0000);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", out);
        return out.toByteArray();
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
