package io.github.demchaav.markdown.theme.tokens;

import java.util.Objects;

/**
 * The complete set of design tokens for a theme — layer one of the theme model.
 *
 * <p>Tokens are pure cosmetic values. Swapping a token bundle reskins the whole
 * document without touching renderers; the {@code with*} methods make it easy to
 * derive a variant from an existing bundle.</p>
 *
 * @param colors     colour palette
 * @param typography fonts and the type scale
 * @param spacing    gaps and paddings
 * @param shape      corner radii and line weights
 * @param page       page geometry
 * @param syntax     code syntax-highlighting colours
 */
public record MarkdownTokens(
        ColorTokens colors,
        TypographyTokens typography,
        SpacingTokens spacing,
        ShapeTokens shape,
        PageTokens page,
        SyntaxColors syntax) {

    /** Validates every token group is present. */
    public MarkdownTokens {
        Objects.requireNonNull(colors, "colors");
        Objects.requireNonNull(typography, "typography");
        Objects.requireNonNull(spacing, "spacing");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(syntax, "syntax");
    }

    /**
     * Creates tokens with the default (light) syntax palette.
     *
     * @param colors     colour palette
     * @param typography fonts and the type scale
     * @param spacing    gaps and paddings
     * @param shape      corner radii and line weights
     * @param page       page geometry
     */
    public MarkdownTokens(ColorTokens colors, TypographyTokens typography, SpacingTokens spacing,
                          ShapeTokens shape, PageTokens page) {
        this(colors, typography, spacing, shape, page, SyntaxColors.defaultLight());
    }

    /**
     * Returns a copy with a different colour palette.
     *
     * @param newColors the replacement palette
     * @return a new {@code MarkdownTokens}
     */
    public MarkdownTokens withColors(ColorTokens newColors) {
        return new MarkdownTokens(newColors, typography, spacing, shape, page, syntax);
    }

    /**
     * Returns a copy with different typography.
     *
     * @param newTypography the replacement typography
     * @return a new {@code MarkdownTokens}
     */
    public MarkdownTokens withTypography(TypographyTokens newTypography) {
        return new MarkdownTokens(colors, newTypography, spacing, shape, page, syntax);
    }

    /**
     * Returns a copy with different spacing.
     *
     * @param newSpacing the replacement spacing
     * @return a new {@code MarkdownTokens}
     */
    public MarkdownTokens withSpacing(SpacingTokens newSpacing) {
        return new MarkdownTokens(colors, typography, newSpacing, shape, page, syntax);
    }

    /**
     * Returns a copy with a different page geometry.
     *
     * @param newPage the replacement page geometry
     * @return a new {@code MarkdownTokens}
     */
    public MarkdownTokens withPage(PageTokens newPage) {
        return new MarkdownTokens(colors, typography, spacing, shape, newPage, syntax);
    }

    /**
     * Returns a copy with different syntax-highlighting colours.
     *
     * @param newSyntax the replacement syntax palette
     * @return a new {@code MarkdownTokens}
     */
    public MarkdownTokens withSyntax(SyntaxColors newSyntax) {
        return new MarkdownTokens(colors, typography, spacing, shape, page, newSyntax);
    }
}
