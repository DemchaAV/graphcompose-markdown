package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The leaf token records expose a {@code withX} per field, so a consumer can override a single
 * token without rebuilding the whole record. Each wither must swap only its target and carry
 * every sibling through unchanged.
 */
class TokenWithersTest {

    private final MarkdownTokens base = DefaultMarkdownTheme.light().tokens();

    @Test
    void colorWitherSwapsOnlyItsTargetField() {
        ColorTokens colors = base.colors();
        DocumentColor magenta = DocumentColor.rgb(255, 0, 255);

        ColorTokens derived = colors.withText(magenta);

        assertThat(derived.text()).isSameAs(magenta);
        assertThat(derived.heading()).isSameAs(colors.heading());
        assertThat(derived.link()).isSameAs(colors.link());
        assertThat(derived.accent()).isSameAs(colors.accent());
        assertThat(derived.surface()).isSameAs(colors.surface());
    }

    @Test
    void surfaceWitherAcceptsNullForNoPageFill() {
        ColorTokens derived = base.colors().withSurface(null);

        assertThat(derived.surface()).isNull();
        assertThat(derived.text()).isSameAs(base.colors().text());
    }

    @Test
    void typographyWithersSwapOnlyTheirTargetField() {
        TypographyTokens typography = base.typography();

        TypographyTokens biggerBody = typography.withBodySize(99.0);
        assertThat(biggerBody.bodySize()).isEqualTo(99.0);
        assertThat(biggerBody.bodyFamily()).isSameAs(typography.bodyFamily());
        assertThat(biggerBody.headingSizes()).isEqualTo(typography.headingSizes());

        TypographyTokens reheaded = typography.withHeadingSizes(List.of(40.0, 32.0, 26.0, 22.0, 18.0, 15.0));
        assertThat(reheaded.headingSize(1)).isEqualTo(40.0);
        assertThat(reheaded.bodySize()).isEqualTo(typography.bodySize());
        assertThat(reheaded.codeFamily()).isSameAs(typography.codeFamily());
    }
}
