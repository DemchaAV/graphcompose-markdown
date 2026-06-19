package io.github.demchaav.markdown.theme.packs;

import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.ColorTokens;
import io.github.demchaav.markdown.theme.tokens.FontFamily;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;
import io.github.demchaav.markdown.theme.tokens.TypographyTokens;

import java.util.List;

/**
 * A minimal theme pack — monochrome, generous whitespace, hairline rules and
 * near-invisible panel fills. Lets the content speak.
 */
public final class MinimalTheme {

    private MinimalTheme() {
    }

    /**
     * The minimal theme (light).
     *
     * @return a new theme instance
     */
    public static MarkdownTheme light() {
        TypographyTokens typography = new TypographyTokens(
                FontFamily.SANS, FontFamily.SANS, FontFamily.MONO,
                10.5, 9.0, 1.5,
                List.of(22.0, 17.0, 14.0, 12.0, 11.0, 10.5));
        ColorTokens colors = new ColorTokens(
                DocumentColor.rgb(33, 33, 33),     // text
                DocumentColor.rgb(150, 150, 150),  // muted
                DocumentColor.rgb(0, 0, 0),        // heading
                DocumentColor.rgb(33, 33, 33),     // link (underlined, no colour)
                DocumentColor.rgb(33, 33, 33),     // code
                DocumentColor.rgb(248, 248, 248),  // codeBackground
                DocumentColor.rgb(255, 255, 255),  // tableRowBackground
                DocumentColor.rgb(220, 220, 220),  // quoteBar
                DocumentColor.rgb(90, 90, 90),     // quoteText
                DocumentColor.rgb(232, 232, 232),  // rule
                DocumentColor.rgb(33, 33, 33),     // accent
                null);                             // surface
        return DefaultMarkdownTheme.of(new MarkdownTokens(
                colors, typography, PackSupport.spacing(14.0, 16.0), PackSupport.shape(2.0, 0.5), PackSupport.a4Page(64.0)));
    }
}
