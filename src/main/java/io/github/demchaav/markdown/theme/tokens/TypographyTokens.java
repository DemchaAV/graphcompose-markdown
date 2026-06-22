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

    /**
     * Returns a copy with a different code font family.
     *
     * @param newCodeFamily the replacement code family
     * @return a new {@code TypographyTokens}
     */
    public TypographyTokens withCodeFamily(FontFamily newCodeFamily) {
        return new TypographyTokens(bodyFamily, headingFamily, newCodeFamily, bodySize, codeSize, lineSpacing,
                headingSizes);
    }

    /**
     * Returns a copy with a different body font family.
     *
     * @param newBodyFamily the replacement body family
     * @return a new {@code TypographyTokens}
     */
    public TypographyTokens withBodyFamily(FontFamily newBodyFamily) {
        return new TypographyTokens(newBodyFamily, headingFamily, codeFamily, bodySize, codeSize, lineSpacing,
                headingSizes);
    }

    /**
     * Returns a copy with a different heading font family.
     *
     * @param newHeadingFamily the replacement heading family
     * @return a new {@code TypographyTokens}
     */
    public TypographyTokens withHeadingFamily(FontFamily newHeadingFamily) {
        return new TypographyTokens(bodyFamily, newHeadingFamily, codeFamily, bodySize, codeSize, lineSpacing,
                headingSizes);
    }

    /**
     * Returns a copy with a different body font size.
     *
     * @param newBodySize the replacement body size in points
     * @return a new {@code TypographyTokens}
     */
    public TypographyTokens withBodySize(double newBodySize) {
        return new TypographyTokens(bodyFamily, headingFamily, codeFamily, newBodySize, codeSize, lineSpacing,
                headingSizes);
    }

    /**
     * Returns a copy with a different code font size.
     *
     * @param newCodeSize the replacement code size in points
     * @return a new {@code TypographyTokens}
     */
    public TypographyTokens withCodeSize(double newCodeSize) {
        return new TypographyTokens(bodyFamily, headingFamily, codeFamily, bodySize, newCodeSize, lineSpacing,
                headingSizes);
    }

    /**
     * Returns a copy with a different body line-spacing multiplier.
     *
     * @param newLineSpacing the replacement line-spacing multiplier (1.0 = single)
     * @return a new {@code TypographyTokens}
     */
    public TypographyTokens withLineSpacing(double newLineSpacing) {
        return new TypographyTokens(bodyFamily, headingFamily, codeFamily, bodySize, codeSize, newLineSpacing,
                headingSizes);
    }

    /**
     * Returns a copy with different heading sizes.
     *
     * @param newHeadingSizes the replacement six heading sizes (h1…h6) in points
     * @return a new {@code TypographyTokens}
     */
    public TypographyTokens withHeadingSizes(List<Double> newHeadingSizes) {
        return new TypographyTokens(bodyFamily, headingFamily, codeFamily, bodySize, codeSize, lineSpacing,
                newHeadingSizes);
    }
}
