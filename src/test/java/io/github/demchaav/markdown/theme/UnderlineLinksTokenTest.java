package io.github.demchaav.markdown.theme;

import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;
import io.github.demchaav.markdown.theme.tokens.ShapeTokens;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Link underlining is now a {@link ShapeTokens} flag (default {@code true}), so a theme can opt
 * for coloured-but-not-underlined links without replacing the style layer.
 */
class UnderlineLinksTokenTest {

    @Test
    void defaultThemeUnderlinesLinks() {
        MarkdownStyles styles = new MarkdownStyles(DefaultMarkdownTheme.light().tokens());

        assertThat(styles.paragraphInline().underlineLinks()).isTrue();
        assertThat(styles.headingInline(1).underlineLinks()).isTrue();
    }

    @Test
    void themeCanTurnOffLinkUnderlining() {
        MarkdownTokens base = DefaultMarkdownTheme.light().tokens();
        ShapeTokens noUnderline =
                new ShapeTokens(base.shape().panelCornerRadius(), base.shape().ruleThickness(), false);
        MarkdownTokens tokens = new MarkdownTokens(
                base.colors(), base.typography(), base.spacing(), noUnderline, base.page(), base.syntax());

        MarkdownStyles styles = new MarkdownStyles(tokens);

        assertThat(styles.paragraphInline().underlineLinks()).isFalse();
        assertThat(styles.headingInline(1).underlineLinks()).isFalse();
    }
}
