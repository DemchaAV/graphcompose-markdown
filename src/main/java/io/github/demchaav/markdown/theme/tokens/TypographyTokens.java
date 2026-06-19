package io.github.demchaav.markdown.theme.tokens;

import java.util.List;
import java.util.Objects;

/**
 * Typography tokens — font families and the type scale.
 *
 * @param bodyFamily    family for body text
 * @param headingFamily family for headings
 * @param codeFamily    family for code (should be {@link FontFamily#MONO})
 * @param bodySize      body font size in points
 * @param codeSize      code font size in points
 * @param lineSpacing   body line-spacing multiplier (1.0 = single)
 * @param headingSizes  the six heading sizes (h1…h6) in points
 */
public record TypographyTokens(
        FontFamily bodyFamily,
        FontFamily headingFamily,
        FontFamily codeFamily,
        double bodySize,
        double codeSize,
        double lineSpacing,
        List<Double> headingSizes) {

    /** Validates the families and that exactly six heading sizes are supplied. */
    public TypographyTokens {
        Objects.requireNonNull(bodyFamily, "bodyFamily");
        Objects.requireNonNull(headingFamily, "headingFamily");
        Objects.requireNonNull(codeFamily, "codeFamily");
        headingSizes = List.copyOf(Objects.requireNonNull(headingSizes, "headingSizes"));
        if (headingSizes.size() != 6) {
            throw new IllegalArgumentException("headingSizes must have 6 entries (h1..h6), was " + headingSizes.size());
        }
    }

    /**
     * Returns the font size for a heading level.
     *
     * @param level the heading level, 1 through 6
     * @return the size in points
     */
    public double headingSize(int level) {
        return headingSizes.get(Math.max(1, Math.min(6, level)) - 1);
    }
}
