package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.document.style.DocumentColor;

import java.util.Objects;

/**
 * Running-footer tokens — an optional page footer. Its {@code left} / {@code center} /
 * {@code right} text may use the placeholder tokens {@code {page}}, {@code {pages}} and
 * {@code {date}}, which the PDF backend substitutes per page (so {@code "Page {page} of {pages}"}
 * renders as {@code "Page 3 of 12"}).
 *
 * <p><b>Disabled by default</b> ({@link #disabled()}) so single-page and screen-oriented output
 * stays clean; enable it with {@link #pageNumbers()} (or a custom instance) via
 * {@code tokens().withFooter(...)}.</p>
 *
 * @param enabled         whether a footer is drawn at all
 * @param leftText        left-aligned footer text, or {@code null}/blank for none
 * @param centerText      centre-aligned footer text, or {@code null}/blank for none
 * @param rightText       right-aligned footer text, or {@code null}/blank for none
 * @param fontSize        footer font size in points
 * @param color           footer text colour
 * @param showOnFirstPage whether the footer is drawn on the first page
 */
public record FooterTokens(
        boolean enabled,
        String leftText,
        String centerText,
        String rightText,
        double fontSize,
        DocumentColor color,
        boolean showOnFirstPage) {

    /** Default footer text/colour used by the factory methods. */
    private static final String DEFAULT_PAGE_TEMPLATE = "Page {page} of {pages}";
    private static final DocumentColor DEFAULT_COLOR = DocumentColor.rgb(120, 120, 120);
    private static final double DEFAULT_FONT_SIZE = 9.0;

    /** Validates the colour is present. */
    public FooterTokens {
        Objects.requireNonNull(color, "color");
    }

    /**
     * A disabled footer (no chrome) — the default for every built-in theme, so existing output is
     * unchanged.
     *
     * @return a disabled footer
     */
    public static FooterTokens disabled() {
        return new FooterTokens(false, null, null, null, DEFAULT_FONT_SIZE, DEFAULT_COLOR, true);
    }

    /**
     * An enabled, centred {@code "Page N of M"} footer, shown on every page.
     *
     * @return a page-number footer with sensible defaults
     */
    public static FooterTokens pageNumbers() {
        return new FooterTokens(true, null, DEFAULT_PAGE_TEMPLATE, null,
                DEFAULT_FONT_SIZE, DEFAULT_COLOR, true);
    }

    /**
     * @param newEnabled whether the footer is drawn
     * @return a copy toggling {@code enabled}
     */
    public FooterTokens withEnabled(boolean newEnabled) {
        return new FooterTokens(newEnabled, leftText, centerText, rightText, fontSize, color, showOnFirstPage);
    }

    /**
     * @param newCenterText the centre footer text (placeholder tokens allowed)
     * @return a copy with different centre text
     */
    public FooterTokens withCenterText(String newCenterText) {
        return new FooterTokens(enabled, leftText, newCenterText, rightText, fontSize, color, showOnFirstPage);
    }

    /**
     * @param newColor the footer text colour
     * @return a copy with a different colour
     */
    public FooterTokens withColor(DocumentColor newColor) {
        return new FooterTokens(enabled, leftText, centerText, rightText, fontSize, newColor, showOnFirstPage);
    }

    /**
     * @param newShowOnFirstPage whether to draw the footer on the first page
     * @return a copy toggling first-page visibility
     */
    public FooterTokens withShowOnFirstPage(boolean newShowOnFirstPage) {
        return new FooterTokens(enabled, leftText, centerText, rightText, fontSize, color, newShowOnFirstPage);
    }
}
