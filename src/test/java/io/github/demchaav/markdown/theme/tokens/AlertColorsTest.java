package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The alert / callout accents are now a token group. These tests pin the default palette to
 * the exact colours the renderers used before the extraction (so existing themes render
 * identically) and check the palette threads through {@link MarkdownTokens}.
 */
class AlertColorsTest {

    @Test
    void defaultPaletteMatchesTheAccentsTheRenderersUsedBefore() {
        AlertColors palette = AlertColors.defaultPalette();

        // GitHub-alert accents (formerly hard-coded in AlertRenderer.accentFor).
        assertThat(palette.note().color()).isEqualTo(DocumentColor.rgb(9, 105, 218).color());
        assertThat(palette.tip().color()).isEqualTo(DocumentColor.rgb(26, 127, 55).color());
        assertThat(palette.important().color()).isEqualTo(DocumentColor.rgb(130, 80, 223).color());
        assertThat(palette.warning().color()).isEqualTo(DocumentColor.rgb(154, 103, 0).color());
        assertThat(palette.caution().color()).isEqualTo(DocumentColor.rgb(207, 34, 46).color());

        // ::: callout accents (formerly hard-coded in CalloutRenderer.accentFor).
        assertThat(palette.calloutInfo().color()).isEqualTo(DocumentColor.rgb(37, 99, 235).color());
        assertThat(palette.calloutWarning().color()).isEqualTo(DocumentColor.rgb(217, 119, 6).color());
        assertThat(palette.calloutError().color()).isEqualTo(DocumentColor.rgb(220, 38, 38).color());
        assertThat(palette.calloutSuccess().color()).isEqualTo(DocumentColor.rgb(22, 163, 74).color());
    }

    @Test
    void tokensDefaultToTheDefaultAlertPalette() {
        // DefaultMarkdownTheme builds tokens through the convenience constructors, which must
        // supply the default palette so a theme written before this group existed is unchanged.
        MarkdownTokens tokens = DefaultMarkdownTheme.light().tokens();
        assertThat(tokens.alertColors()).isNotNull();
        assertThat(tokens.alertColors().note().color())
                .isEqualTo(AlertColors.defaultPalette().note().color());
    }

    @Test
    void withAlertColorsReplacesOnlyThatGroup() {
        MarkdownTokens base = DefaultMarkdownTheme.light().tokens();
        AlertColors custom = new AlertColors(
                DocumentColor.rgb(1, 2, 3), DocumentColor.rgb(1, 2, 3), DocumentColor.rgb(1, 2, 3),
                DocumentColor.rgb(1, 2, 3), DocumentColor.rgb(1, 2, 3), DocumentColor.rgb(1, 2, 3),
                DocumentColor.rgb(1, 2, 3), DocumentColor.rgb(1, 2, 3), DocumentColor.rgb(1, 2, 3));

        MarkdownTokens derived = base.withAlertColors(custom);

        assertThat(derived.alertColors()).isSameAs(custom);
        // Every other group is preserved.
        assertThat(derived.colors()).isSameAs(base.colors());
        assertThat(derived.typography()).isSameAs(base.typography());
        assertThat(derived.syntax()).isSameAs(base.syntax());
    }
}
