package io.github.demchaav.markdown.theme.style;

import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * {@link MarkdownStyles#lineLeading(double)} converts the typography line-spacing <em>multiplier</em>
 * into the absolute extra leading (points) the engine's points-based {@code lineSpacing} expects.
 * This is the guard for the line-spacing fix: passing the raw multiplier would make this go red.
 */
class MarkdownStylesTest {

    private static final MarkdownTokens TOKENS = DefaultMarkdownTheme.light().tokens();

    @Test
    void lineLeadingConvertsMultiplierToExtraPoints() {
        // The default light theme uses a 1.4 multiplier, so leading == (1.4 - 1.0) * fontSize.
        MarkdownStyles styles = new MarkdownStyles(TOKENS);
        assertThat(styles.lineLeading(11.0)).isCloseTo(4.4, within(1e-9));
        assertThat(styles.lineLeading(10.0)).isCloseTo(4.0, within(1e-9));
    }

    @Test
    void singleAndSubSingleSpacingYieldNoExtraLeading() {
        MarkdownTokens single = TOKENS.withTypography(TOKENS.typography().withLineSpacing(1.0));
        assertThat(new MarkdownStyles(single).lineLeading(11.0)).isEqualTo(0.0);

        // A multiplier below 1.0 clamps to 0 rather than pulling lines together.
        MarkdownTokens tight = TOKENS.withTypography(TOKENS.typography().withLineSpacing(0.5));
        assertThat(new MarkdownStyles(tight).lineLeading(11.0)).isEqualTo(0.0);
    }
}
