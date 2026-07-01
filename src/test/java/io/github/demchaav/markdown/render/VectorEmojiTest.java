package io.github.demchaav.markdown.render;

import com.demcha.compose.document.emoji.EmojiLibrary;
import com.demcha.compose.document.node.ExternalLinkTarget;
import com.demcha.compose.document.node.InlineImageRun;
import com.demcha.compose.document.node.InlineSvgRun;
import com.demcha.compose.document.node.InlineTextRun;
import com.demcha.compose.document.dsl.RichText;
import io.github.demchaav.markdown.model.inline.EmojiRun;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Emoji shortcodes resolve in priority order: a theme {@code EmojiResolver} image wins, else a
 * vector colour glyph from the optional {@code graph-compose-emoji} artifact (on the test
 * classpath), else literal {@code :shortcode:} text in the surrounding style.
 */
class VectorEmojiTest {

    private static final InlineStyle BASE =
            new MarkdownStyles(DefaultMarkdownTheme.light().tokens()).paragraphInline();

    @Test
    void knownShortcodeWithoutAResolverBecomesAnInlineSvgRun() {
        assertThat(EmojiLibrary.getDefault().isAvailable())
                .as("graph-compose-emoji is on the test classpath").isTrue();

        RichText rich = new InlineRenderer().render(List.of(new EmojiRun("rocket")), BASE);

        assertThat(rich.runs()).hasSize(1);
        assertThat(rich.runs().get(0)).isInstanceOf(InlineSvgRun.class);
    }

    @Test
    void unknownShortcodeFallsBackToStyledLiteralText() {
        RichText rich = new InlineRenderer().render(
                List.of(new EmojiRun("definitelynotanemoji")), BASE);

        assertThat(rich.runs()).hasSize(1);
        InlineTextRun run = (InlineTextRun) rich.runs().get(0);
        assertThat(run.text()).isEqualTo(":definitelynotanemoji:");
        // The fallback keeps the surrounding base style (not some engine default).
        assertThat(run.textStyle().size()).isEqualTo(BASE.size());
    }

    @Test
    void aResolverImageStillWinsOverTheVectorGlyph() throws Exception {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB), "png", png);
        InlineRenderer renderer = new InlineRenderer(shortcode -> Optional.of(png.toByteArray()));

        RichText rich = renderer.render(List.of(new EmojiRun("rocket")), BASE);

        assertThat(rich.runs()).hasSize(1);
        assertThat(rich.runs().get(0)).isInstanceOf(InlineImageRun.class); // image, not SVG
    }

    @Test
    void aVectorEmojiInsideALinkCarriesTheLinkAnnotation() {
        List<InlineNode> nodes = List.of(new LinkRun("https://example.com", null,
                List.of(new EmojiRun("rocket"))));

        RichText rich = new InlineRenderer().render(nodes, BASE);

        InlineSvgRun svg = rich.runs().stream()
                .filter(InlineSvgRun.class::isInstance).map(InlineSvgRun.class::cast)
                .findFirst().orElseThrow();
        assertThat(svg.linkTarget())
                .as("the surrounding external link is threaded onto the glyph")
                .isInstanceOfSatisfying(ExternalLinkTarget.class, target ->
                        assertThat(target.options().uri()).isEqualTo("https://example.com"));
    }
}
