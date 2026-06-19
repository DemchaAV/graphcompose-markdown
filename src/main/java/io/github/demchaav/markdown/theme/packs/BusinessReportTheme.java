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
 * A business-report theme pack — serif headings over a sans body, a deep navy
 * heading colour and a teal accent, tuned for structured corporate documents.
 */
public final class BusinessReportTheme {

    private BusinessReportTheme() {
    }

    /**
     * The business-report theme (light).
     *
     * @return a new theme instance
     */
    public static MarkdownTheme light() {
        TypographyTokens typography = new TypographyTokens(
                FontFamily.SANS, FontFamily.SERIF, FontFamily.MONO,
                11.0, 9.5, 1.4,
                List.of(23.0, 18.0, 15.0, 13.0, 11.5, 11.0));
        ColorTokens colors = new ColorTokens(
                DocumentColor.rgb(40, 44, 52),     // text
                DocumentColor.rgb(110, 116, 128),  // muted
                DocumentColor.rgb(20, 28, 46),     // heading (deep navy)
                DocumentColor.rgb(0, 90, 160),     // link
                DocumentColor.rgb(40, 44, 52),     // code
                DocumentColor.rgb(241, 244, 248),  // codeBackground
                DocumentColor.rgb(255, 255, 255),  // tableRowBackground
                DocumentColor.rgb(0, 120, 140),    // quoteBar (teal)
                DocumentColor.rgb(70, 76, 88),     // quoteText
                DocumentColor.rgb(214, 220, 228),  // rule
                DocumentColor.rgb(0, 120, 140),    // accent (teal)
                null);                             // surface
        return DefaultMarkdownTheme.of(new MarkdownTokens(
                colors, typography, PackSupport.spacing(11.0, 18.0), PackSupport.shape(4.0, 1.0), PackSupport.a4Page(56.0)));
    }
}
