package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.node.InlineHighlightRun;
import com.demcha.compose.document.node.InlineImageRun;
import com.demcha.compose.document.node.InlineRun;
import com.demcha.compose.document.node.InlineShapeRun;
import com.demcha.compose.document.node.InlineTextRun;
import com.demcha.compose.document.node.InternalLinkTarget;
import io.github.demchaav.markdown.extension.ImageResolver;
import io.github.demchaav.markdown.model.inline.CodeRun;
import io.github.demchaav.markdown.model.inline.FootnoteRefRun;
import io.github.demchaav.markdown.model.inline.ImageRun;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.model.inline.TextRun;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * An inline image (one that sits amid other inline content, so it is an {@link ImageRun}
 * rather than a block image) resolves to a real inline image; only an unresolved source
 * falls back to alt text.
 */
class InlineRendererTest {

    private static final InlineStyle BASE =
            new MarkdownStyles(DefaultMarkdownTheme.light().tokens()).paragraphInline();

    @Test
    void resolvedInlineImageBecomesAnImageRunPreservingAspectRatio() {
        byte[] wideBadge = png(4, 2); // width:height == 2:1
        InlineRenderer renderer = new InlineRenderer(null, source -> Optional.of(wideBadge));

        RichText rich = renderer.render(
                List.of(new TextRun("see "), new ImageRun("badge.png", "alt", null), new TextRun(" ok")),
                BASE);

        List<InlineRun> runs = rich.runs();
        assertThat(runs).hasSize(3);
        assertThat(runs.get(0)).isInstanceOf(InlineTextRun.class);
        assertThat(runs.get(1)).isInstanceOf(InlineImageRun.class);
        assertThat(runs.get(2)).isInstanceOf(InlineTextRun.class);

        InlineImageRun image = (InlineImageRun) runs.get(1);
        assertThat(image.height()).isEqualTo(BASE.size());
        assertThat(image.width()).isCloseTo(BASE.size() * 2.0, within(0.001)); // aspect preserved
    }

    @Test
    void unresolvedInlineImageFallsBackToAltText() {
        // No resolver: the image cannot be loaded, so the alt text is rendered (not dropped).
        InlineRenderer renderer = new InlineRenderer();

        RichText rich = renderer.render(
                List.of(new ImageRun("missing.png", "alt text", null)), BASE);

        assertThat(rich.runs()).hasSize(1);
        assertThat(rich.runs().get(0)).isInstanceOf(InlineTextRun.class);
        assertThat(((InlineTextRun) rich.runs().get(0)).text()).isEqualTo("alt text");
    }

    @Test
    void inlineImageInsideALinkCarriesTheLinkAnnotation() {
        byte[] icon = png(2, 2);
        InlineRenderer renderer = new InlineRenderer(null, source -> Optional.of(icon));

        RichText rich = renderer.render(
                List.of(new LinkRun("https://example.com", null,
                        List.of(new ImageRun("icon.png", "", null)))),
                BASE);

        InlineImageRun image = (InlineImageRun) rich.runs().stream()
                .filter(InlineImageRun.class::isInstance).findFirst().orElseThrow();
        assertThat(image.linkOptions()).isNotNull(); // the surrounding link is threaded onto the image
    }

    @Test
    void internalAnchorLinkBecomesAnInternalLinkTarget() {
        // [jump](#My Heading) -> a native in-document jump to the heading's slug "my-heading".
        InlineRenderer renderer = new InlineRenderer();

        RichText rich = renderer.render(
                List.of(new LinkRun("#My Heading", null, List.of(new TextRun("jump")))), BASE);

        assertThat(rich.runs()).hasSize(1);
        InlineTextRun run = (InlineTextRun) rich.runs().get(0);
        assertThat(run.text()).isEqualTo("jump");
        assertThat(run.linkTarget()).isInstanceOf(InternalLinkTarget.class);
        assertThat(((InternalLinkTarget) run.linkTarget()).anchor()).isEqualTo("my-heading");
    }

    @Test
    void footnoteReferenceLinksToTheNoteAnchor() {
        InlineRenderer renderer = new InlineRenderer();

        RichText rich = renderer.render(
                List.of(new TextRun("cite"), new FootnoteRefRun(2)), BASE);

        InlineTextRun marker = rich.runs().stream()
                .filter(InlineTextRun.class::isInstance).map(InlineTextRun.class::cast)
                .filter(r -> r.text().equals("[2]")).findFirst().orElseThrow();
        assertThat(marker.linkTarget()).isInstanceOf(InternalLinkTarget.class);
        assertThat(((InternalLinkTarget) marker.linkTarget()).anchor()).isEqualTo("fn-2");
    }

    @Test
    void inlineCodeBecomesARoundedChip() {
        InlineRenderer renderer = new InlineRenderer();

        RichText rich = renderer.render(List.of(new CodeRun("x = 1")), BASE);

        assertThat(rich.runs()).hasSize(1);
        assertThat(rich.runs().get(0)).isInstanceOf(InlineHighlightRun.class);
    }

    @Test
    void geometricEmojiInInlineCodeBecomesAShapeNotAQuestionMark() {
        // `🔴` has no glyph in a PDF mono font; inside inline code it must still become a vector
        // shape (forgoing the chip) rather than a missing-glyph "?".
        InlineRenderer renderer = new InlineRenderer();

        RichText justEmoji = renderer.render(List.of(new CodeRun("🔴")), BASE);
        assertThat(justEmoji.runs()).hasSize(1);
        assertThat(justEmoji.runs().get(0)).isInstanceOf(InlineShapeRun.class);

        RichText emojiThenText = renderer.render(List.of(new CodeRun("🔴 down")), BASE);
        assertThat(emojiThenText.runs().get(0)).isInstanceOf(InlineShapeRun.class);   // the dot
        assertThat(emojiThenText.runs().get(1)).isInstanceOf(InlineTextRun.class);    // " down"
    }

    @Test
    void inlineCodeWithoutAChipColourFallsBackToPlainText() {
        // A hand-built InlineStyle with no chip colour must not attempt a highlight chip.
        InlineStyle noChip = new InlineStyle(BASE.family(), BASE.size(), BASE.color(), false, false,
                BASE.codeFamily(), BASE.codeSize(), BASE.codeColor(), null, BASE.linkColor(), BASE.underlineLinks());

        RichText rich = new InlineRenderer().render(List.of(new CodeRun("x = 1")), noChip);

        assertThat(rich.runs()).hasSize(1);
        assertThat(rich.runs().get(0)).isInstanceOf(InlineTextRun.class);
    }

    @Test
    void externalLinkStillCarriesAUriTarget() {
        InlineRenderer renderer = new InlineRenderer();

        RichText rich = renderer.render(
                List.of(new LinkRun("https://example.com", null, List.of(new TextRun("site")))), BASE);

        InlineTextRun run = (InlineTextRun) rich.runs().get(0);
        assertThat(run.linkOptions()).as("external link keeps its URI options").isNotNull();
    }

    private static byte[] png(int width, int height) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
