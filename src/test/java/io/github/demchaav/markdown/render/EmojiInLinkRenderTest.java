package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.node.InlineShapeRun;
import com.demcha.compose.document.node.InlineTextRun;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.model.inline.TextRun;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression: the link branch of {@link InlineRenderer} used to return before the geometric-emoji
 * shape mapping, so an emoji typed inside link text (e.g. {@code [🔴 Critical](url)}) rendered as a
 * missing glyph. It must become a shape, with the surrounding words kept as text.
 */
class EmojiInLinkRenderTest {

    private static final InlineStyle BASE =
            new MarkdownStyles(DefaultMarkdownTheme.light().tokens()).paragraphInline();

    @Test
    void geometricEmojiInsideLinkTextBecomesAShape() {
        RichText rich = new InlineRenderer().render(
                List.of(new LinkRun("https://example.com", null, List.of(new TextRun("🔴 critical")))),
                BASE);

        assertThat(rich.runs()).anySatisfy(r -> assertThat(r).isInstanceOf(InlineShapeRun.class));
        assertThat(rich.runs()).anySatisfy(r -> assertThat(r).isInstanceOf(InlineTextRun.class));
    }
}
