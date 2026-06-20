package io.github.demchaav.markdown.extension;

/**
 * The kind of a highlighted code token. A theme maps each kind to a colour; the
 * regex highlighter emits a practical subset, with everything it does not
 * recognise left {@link #PLAIN}.
 */
public enum CodeTokenType {
    /** Uncoloured code (default text colour). */
    PLAIN,
    /** Language keyword (e.g. {@code if}, {@code class}, {@code return}). */
    KEYWORD,
    /** String / character literal. */
    STRING,
    /** Comment. */
    COMMENT,
    /** Numeric literal. */
    NUMBER,
    /** Annotation / decorator (e.g. {@code @Override}). */
    ANNOTATION,
    /** A name in call position ({@code foo(}). */
    FUNCTION
}
