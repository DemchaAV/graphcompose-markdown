package io.github.demchaav.markdown.theme.packs;

import com.demcha.compose.document.api.DocumentPageSize;
import com.demcha.compose.document.style.DocumentInsets;
import io.github.demchaav.markdown.theme.tokens.PageTokens;
import io.github.demchaav.markdown.theme.tokens.ShapeTokens;
import io.github.demchaav.markdown.theme.tokens.SpacingTokens;

/**
 * Shared token scaffolding for the built-in theme packs — the bits that are
 * tedious or error-prone to repeat (page geometry) plus sensible spacing/shape
 * defaults each pack can reuse or replace.
 */
final class PackSupport {

    /** A4 width in points (210&nbsp;mm). */
    static final double A4_WIDTH = 595.2755905511811;

    private PackSupport() {
    }

    /**
     * A4 page tokens with a uniform margin; full-width elements stay one point
     * inside the content box to absorb the engine's sub-point rounding.
     *
     * @param margin uniform page margin in points
     * @return the page tokens
     */
    static PageTokens a4Page(double margin) {
        return new PageTokens(
                DocumentPageSize.A4,
                new DocumentInsets(margin, margin, margin, margin),
                A4_WIDTH - 2 * margin - 1.0);
    }

    /**
     * Spacing tokens with a configurable block gap and list indent; the
     * remaining paddings use balanced defaults.
     *
     * @param blockSpacing vertical gap between blocks
     * @param listIndent   indent per list nesting level
     * @return the spacing tokens
     */
    static SpacingTokens spacing(double blockSpacing, double listIndent) {
        return new SpacingTokens(
                blockSpacing, 8.0, 4.0, 4.0, listIndent,
                12.0, 10.0, 3.0, 12.0, 4.0, 6.0);
    }

    /**
     * Shape tokens.
     *
     * @param cornerRadius panel corner radius
     * @param ruleThickness rule thickness
     * @return the shape tokens
     */
    static ShapeTokens shape(double cornerRadius, double ruleThickness) {
        return new ShapeTokens(cornerRadius, ruleThickness);
    }
}
