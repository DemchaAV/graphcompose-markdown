package io.github.demchaav.markdown.theme;

import com.demcha.compose.document.api.DocumentPageSize;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentInsets;
import io.github.demchaav.markdown.render.BuiltinRenderers;
import io.github.demchaav.markdown.theme.tokens.ColorTokens;
import io.github.demchaav.markdown.theme.tokens.FontFamily;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;
import io.github.demchaav.markdown.theme.tokens.PageTokens;
import io.github.demchaav.markdown.theme.tokens.ShapeTokens;
import io.github.demchaav.markdown.theme.tokens.SpacingTokens;
import io.github.demchaav.markdown.theme.tokens.TypographyTokens;

import java.util.List;

/**
 * The library's first-party theme, in light and dark variants.
 *
 * <p>Both variants use the PDF base-14 fonts (Helvetica for text, Courier for
 * code), so the default theme needs no bundled font artifact. Derive your own
 * theme from either variant with {@link MarkdownTheme#builder(MarkdownTheme)}.</p>
 */
public final class DefaultMarkdownTheme {

    private static final double A4_WIDTH = 595.2755905511811;
    private static final double MARGIN = 56.0;
    // Full-width elements (rules, images) stay a hair inside the content box to
    // absorb the engine's sub-point rounding of the available width.
    private static final double CONTENT_SAFETY_INSET = 1.0;

    private DefaultMarkdownTheme() {
    }

    /**
     * The light variant: dark text on a white page.
     *
     * @return a new theme instance
     */
    public static MarkdownTheme light() {
        return of(lightTokens());
    }

    /**
     * The dark variant: light text on a dark page (full-page background).
     *
     * @return a new theme instance
     */
    public static MarkdownTheme dark() {
        return of(darkTokens());
    }

    /**
     * Assembles a theme from the given tokens using the built-in renderers.
     *
     * <p>This is the shared entry point theme packs use: supply a token bundle
     * and get a theme wired with the standard {@code NodeRenderer}s. Override
     * individual renderers afterwards with {@link MarkdownTheme#builder(MarkdownTheme)}.</p>
     *
     * @param tokens the design tokens
     * @return a theme with the standard renderers and the given tokens
     */
    public static MarkdownTheme of(MarkdownTokens tokens) {
        MarkdownTheme.Builder builder = MarkdownTheme.builder().tokens(tokens);
        BuiltinRenderers.registerDefaults(builder.registry());
        return builder.build();
    }

    private static TypographyTokens typography() {
        return new TypographyTokens(
                FontFamily.SANS, FontFamily.SANS, FontFamily.MONO,
                11.0, 9.5, 1.35,
                List.of(26.0, 21.0, 17.0, 14.0, 12.0, 11.0));
    }

    private static SpacingTokens spacing() {
        return new SpacingTokens(
                10.0,  // blockSpacing
                8.0,   // headingSpaceAbove
                4.0,   // headingSpaceBelow
                4.0,   // listItemSpacing
                18.0,  // listIndent
                12.0,  // codePadding
                10.0,  // quotePadding
                3.0,   // quoteAccentWidth
                12.0,  // calloutPadding
                4.0,   // calloutAccentWidth
                6.0);  // tableCellPadding
    }

    private static ShapeTokens shape() {
        return new ShapeTokens(6.0, 1.0);
    }

    private static PageTokens page() {
        return new PageTokens(
                DocumentPageSize.A4,
                new DocumentInsets(MARGIN, MARGIN, MARGIN, MARGIN),
                A4_WIDTH - 2 * MARGIN - CONTENT_SAFETY_INSET);
    }

    private static MarkdownTokens lightTokens() {
        ColorTokens colors = new ColorTokens(
                DocumentColor.rgb(33, 37, 41),     // text
                DocumentColor.rgb(108, 117, 125),  // muted
                DocumentColor.rgb(17, 24, 39),     // heading
                DocumentColor.rgb(13, 110, 253),   // link
                DocumentColor.rgb(45, 55, 72),     // code
                DocumentColor.rgb(243, 244, 246),  // codeBackground
                DocumentColor.rgb(255, 255, 255),  // tableRowBackground
                DocumentColor.rgb(148, 163, 184),  // quoteBar
                DocumentColor.rgb(82, 90, 100),    // quoteText
                DocumentColor.rgb(222, 226, 230),  // rule
                DocumentColor.rgb(13, 110, 253),   // accent
                null);                             // surface (white page)
        return new MarkdownTokens(colors, typography(), spacing(), shape(), page());
    }

    private static MarkdownTokens darkTokens() {
        ColorTokens colors = new ColorTokens(
                DocumentColor.rgb(222, 226, 230),  // text
                DocumentColor.rgb(148, 163, 184),  // muted
                DocumentColor.rgb(241, 245, 249),  // heading
                DocumentColor.rgb(96, 165, 250),   // link
                DocumentColor.rgb(226, 232, 240),  // code
                DocumentColor.rgb(30, 41, 59),     // codeBackground
                DocumentColor.rgb(23, 32, 50),     // tableRowBackground
                DocumentColor.rgb(71, 85, 105),    // quoteBar
                DocumentColor.rgb(148, 163, 184),  // quoteText
                DocumentColor.rgb(51, 65, 85),     // rule
                DocumentColor.rgb(96, 165, 250),   // accent
                DocumentColor.rgb(15, 23, 42));    // surface (dark page)
        return new MarkdownTokens(colors, typography(), spacing(), shape(), page());
    }
}
