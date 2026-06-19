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
 * A GitHub-flavoured theme pack — the familiar Primer palette and a sans body
 * with a monospace code face, in light and dark variants.
 */
public final class GitHubTheme {

    private GitHubTheme() {
    }

    private static TypographyTokens typography() {
        return new TypographyTokens(
                FontFamily.SANS, FontFamily.SANS, FontFamily.MONO,
                11.0, 9.5, 1.4,
                List.of(24.0, 19.0, 16.0, 13.5, 12.0, 11.0));
    }

    /**
     * GitHub light.
     *
     * @return a new theme instance
     */
    public static MarkdownTheme light() {
        ColorTokens colors = new ColorTokens(
                DocumentColor.rgb(31, 35, 40),     // text
                DocumentColor.rgb(101, 109, 118),  // muted
                DocumentColor.rgb(31, 35, 40),     // heading
                DocumentColor.rgb(9, 105, 218),    // link
                DocumentColor.rgb(31, 35, 40),     // code
                DocumentColor.rgb(246, 248, 250),  // codeBackground
                DocumentColor.rgb(255, 255, 255),  // tableRowBackground
                DocumentColor.rgb(208, 215, 222),  // quoteBar
                DocumentColor.rgb(101, 109, 118),  // quoteText
                DocumentColor.rgb(216, 222, 228),  // rule
                DocumentColor.rgb(9, 105, 218),    // accent
                null);                             // surface
        return DefaultMarkdownTheme.of(new MarkdownTokens(
                colors, typography(), PackSupport.spacing(10.0, 18.0), PackSupport.shape(6.0, 1.0), PackSupport.a4Page(56.0)));
    }

    /**
     * GitHub dark.
     *
     * @return a new theme instance
     */
    public static MarkdownTheme dark() {
        ColorTokens colors = new ColorTokens(
                DocumentColor.rgb(230, 237, 243),  // text
                DocumentColor.rgb(125, 133, 144),  // muted
                DocumentColor.rgb(230, 237, 243),  // heading
                DocumentColor.rgb(47, 129, 247),   // link
                DocumentColor.rgb(230, 237, 243),  // code
                DocumentColor.rgb(22, 27, 34),     // codeBackground
                DocumentColor.rgb(18, 22, 28),     // tableRowBackground
                DocumentColor.rgb(48, 54, 61),     // quoteBar
                DocumentColor.rgb(125, 133, 144),  // quoteText
                DocumentColor.rgb(48, 54, 61),     // rule
                DocumentColor.rgb(47, 129, 247),   // accent
                DocumentColor.rgb(13, 17, 23));    // surface
        return DefaultMarkdownTheme.of(new MarkdownTokens(
                colors, typography(), PackSupport.spacing(10.0, 18.0), PackSupport.shape(6.0, 1.0), PackSupport.a4Page(56.0)));
    }
}
