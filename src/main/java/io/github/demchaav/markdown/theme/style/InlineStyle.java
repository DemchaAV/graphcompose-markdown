package io.github.demchaav.markdown.theme.style;

import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.theme.tokens.FontFamily;

/**
 * The base style an inline run is rendered against before its own decorations
 * (bold, italic, strikethrough, code, link) are applied.
 *
 * <p>Paragraphs and headings supply different base styles (body vs heading font,
 * size and colour); the inline renderer combines the base with each run's
 * decorations to produce styled segments.</p>
 *
 * @param family         base font family for normal text
 * @param size           base font size in points
 * @param color          base text colour
 * @param baseBold       whether all text is bold by default (e.g. in a heading)
 * @param baseItalic     whether all text is italic by default
 * @param codeFamily     font family for inline code
 * @param codeSize       font size for inline code
 * @param codeColor      colour for inline code
 * @param codeBackground chip fill colour behind inline code
 * @param linkColor      colour for hyperlinks
 * @param underlineLinks whether links are underlined
 */
public record InlineStyle(
        FontFamily family,
        double size,
        DocumentColor color,
        boolean baseBold,
        boolean baseItalic,
        FontFamily codeFamily,
        double codeSize,
        DocumentColor codeColor,
        DocumentColor codeBackground,
        DocumentColor linkColor,
        boolean underlineLinks) {
}
