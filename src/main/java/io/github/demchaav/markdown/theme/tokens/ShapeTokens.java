package io.github.demchaav.markdown.theme.tokens;

/**
 * Shape and line-decoration tokens — corner radii, line weights, and whether links underline.
 *
 * @param panelCornerRadius corner radius for code/callout panels
 * @param ruleThickness     thickness of a horizontal rule
 * @param underlineLinks    whether hyperlinks are underlined (a theme can set {@code false} for
 *                          coloured-but-not-underlined links)
 */
public record ShapeTokens(double panelCornerRadius, double ruleThickness, boolean underlineLinks) {

    /**
     * Creates shape tokens with underlined links (the default).
     *
     * @param panelCornerRadius corner radius for code/callout panels
     * @param ruleThickness     thickness of a horizontal rule
     */
    public ShapeTokens(double panelCornerRadius, double ruleThickness) {
        this(panelCornerRadius, ruleThickness, true);
    }
}
