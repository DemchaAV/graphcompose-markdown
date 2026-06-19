package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.font.FontName;

/**
 * A logical font family that resolves to a concrete {@link FontName} for any
 * bold/italic combination.
 *
 * <p>The three families map onto the PDF base-14 fonts, so the default theme
 * needs no bundled font artifact. Bold and italic are expressed through the
 * font variant (rather than a decoration flag), which lets a renderer combine
 * them freely with strikethrough or underline.</p>
 */
public enum FontFamily {

    /** Sans-serif (Helvetica family). */
    SANS(FontName.HELVETICA, FontName.HELVETICA_BOLD, FontName.HELVETICA_OBLIQUE, FontName.HELVETICA_BOLD_OBLIQUE),
    /** Serif (Times family). */
    SERIF(FontName.TIMES_ROMAN, FontName.TIMES_BOLD, FontName.TIMES_ITALIC, FontName.TIMES_BOLD_ITALIC),
    /** Monospace (Courier family) — used for code. */
    MONO(FontName.COURIER, FontName.COURIER_BOLD, FontName.COURIER_OBLIQUE, FontName.COURIER_BOLD_OBLIQUE);

    private final FontName regular;
    private final FontName bold;
    private final FontName italic;
    private final FontName boldItalic;

    FontFamily(FontName regular, FontName bold, FontName italic, FontName boldItalic) {
        this.regular = regular;
        this.bold = bold;
        this.italic = italic;
        this.boldItalic = boldItalic;
    }

    /**
     * Resolves the concrete font for the requested weight/slant combination.
     *
     * @param bold   whether bold is requested
     * @param italic whether italic is requested
     * @return the matching {@link FontName}
     */
    public FontName resolve(boolean bold, boolean italic) {
        if (bold && italic) {
            return boldItalic;
        }
        if (bold) {
            return this.bold;
        }
        if (italic) {
            return this.italic;
        }
        return regular;
    }
}
