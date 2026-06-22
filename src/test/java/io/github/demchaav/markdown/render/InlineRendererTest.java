package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.node.InlineImageRun;
import com.demcha.compose.document.node.InlineRun;
import com.demcha.compose.document.node.InlineTextRun;
import io.github.demchaav.markdown.extension.ImageResolver;
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
