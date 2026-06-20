package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.document.style.DocumentColor;

import java.util.Objects;

/**
 * Code syntax-highlighting colours — one per highlighted token kind. Plain code
 * uses the theme's {@link ColorTokens#code()} colour, so it is not repeated here.
 *
 * @param keyword    language keywords
 * @param string     string / character literals
 * @param comment    comments
 * @param number     numeric literals
 * @param annotation annotations / decorators
 * @param function   names in call position
 */
public record SyntaxColors(
        DocumentColor keyword,
        DocumentColor string,
        DocumentColor comment,
        DocumentColor number,
        DocumentColor annotation,
        DocumentColor function) {

    /** Validates all colours are present. */
    public SyntaxColors {
        Objects.requireNonNull(keyword, "keyword");
        Objects.requireNonNull(string, "string");
        Objects.requireNonNull(comment, "comment");
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(annotation, "annotation");
        Objects.requireNonNull(function, "function");
    }

    /**
     * A light syntax palette (GitHub-ish): red keywords, blue strings/numbers,
     * grey comments, purple call names.
     *
     * @return the default light palette
     */
    public static SyntaxColors defaultLight() {
        return new SyntaxColors(
                DocumentColor.rgb(207, 34, 46),    // keyword
                DocumentColor.rgb(10, 48, 105),     // string
                DocumentColor.rgb(110, 119, 129),   // comment
                DocumentColor.rgb(5, 80, 174),      // number
                DocumentColor.rgb(149, 56, 0),      // annotation
                DocumentColor.rgb(130, 80, 223));   // function
    }

    /**
     * A dark syntax palette (GitHub-dark-ish): warm keywords, light-blue
     * strings/numbers, grey comments, purple call names.
     *
     * @return the default dark palette
     */
    public static SyntaxColors defaultDark() {
        return new SyntaxColors(
                DocumentColor.rgb(255, 123, 114),   // keyword
                DocumentColor.rgb(165, 214, 255),    // string
                DocumentColor.rgb(139, 148, 158),    // comment
                DocumentColor.rgb(121, 192, 255),    // number
                DocumentColor.rgb(255, 166, 87),     // annotation
                DocumentColor.rgb(210, 168, 255));   // function
    }
}
