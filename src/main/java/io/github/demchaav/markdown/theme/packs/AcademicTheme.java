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
 * An academic theme pack — a serif body with generous leading, restrained
 * headings, wide margins and a muted maroon accent, for papers and reports.
 */
public final class AcademicTheme {

    private AcademicTheme() {
    }

    /**
     * The academic theme (light).
     *
     * @return a new theme instance
     */
    public static MarkdownTheme light() {
        TypographyTokens typography = new TypographyTokens(
                FontFamily.SERIF, FontFamily.SERIF, FontFamily.MONO,
                11.5, 9.5, 1.5,
                List.of(22.0, 17.0, 14.5, 12.5, 11.5, 11.0));
        ColorTokens colors = new ColorTokens(
                DocumentColor.rgb(26, 26, 26),     // text
                DocumentColor.rgb(90, 90, 90),     // muted
                DocumentColor.rgb(15, 15, 15),     // heading
                DocumentColor.rgb(60, 64, 128),    // link
                DocumentColor.rgb(40, 40, 40),     // code
                DocumentColor.rgb(245, 245, 242),  // codeBackground
                DocumentColor.rgb(255, 255, 255),  // tableRowBackground
                DocumentColor.rgb(170, 160, 140),  // quoteBar
                DocumentColor.rgb(70, 70, 70),     // quoteText
                DocumentColor.rgb(210, 206, 198),  // rule
                DocumentColor.rgb(120, 40, 40),    // accent
                null);                             // surface
        return DefaultMarkdownTheme.of(new MarkdownTokens(
                colors, typography, PackSupport.spacing(12.0, 20.0), PackSupport.shape(3.0, 0.75), PackSupport.a4Page(64.0)));
    }
}
