package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.document.style.DocumentColor;

import java.util.Objects;

/**
 * The colour palette of a theme — pure cosmetic tokens.
 *
 * @param text           default body text colour
 * @param muted          secondary/subdued text
 * @param heading        heading text colour
 * @param link           hyperlink colour
 * @param code               inline code text colour
 * @param codeBackground     fenced code block / inline code background (also the quote plate)
 * @param tableRowBackground table body cell background
 * @param quoteBar           blockquote left accent bar
 * @param quoteText          blockquote text colour
 * @param rule               horizontal rule / divider colour
 * @param accent             general accent (default callout bar, emphasis)
 * @param surface            page background; may be {@code null} for no fill
 */
public record ColorTokens(
        DocumentColor text,
        DocumentColor muted,
        DocumentColor heading,
        DocumentColor link,
        DocumentColor code,
        DocumentColor codeBackground,
        DocumentColor tableRowBackground,
        DocumentColor quoteBar,
        DocumentColor quoteText,
        DocumentColor rule,
        DocumentColor accent,
        DocumentColor surface) {

    /** Validates the required (non-{@code surface}) colours are present. */
    public ColorTokens {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(muted, "muted");
        Objects.requireNonNull(heading, "heading");
        Objects.requireNonNull(link, "link");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(codeBackground, "codeBackground");
        Objects.requireNonNull(tableRowBackground, "tableRowBackground");
        Objects.requireNonNull(quoteBar, "quoteBar");
        Objects.requireNonNull(quoteText, "quoteText");
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(accent, "accent");
    }

    /**
     * Returns a copy with a different code-block background.
     *
     * @param newCodeBackground the replacement background colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withCodeBackground(DocumentColor newCodeBackground) {
        return new ColorTokens(text, muted, heading, link, code, newCodeBackground,
                tableRowBackground, quoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different accent colour.
     *
     * @param newAccent the replacement accent colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withAccent(DocumentColor newAccent) {
        return new ColorTokens(text, muted, heading, link, code, codeBackground,
                tableRowBackground, quoteBar, quoteText, rule, newAccent, surface);
    }

    /**
     * Returns a copy with a different body text colour.
     *
     * @param newText the replacement body text colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withText(DocumentColor newText) {
        return new ColorTokens(newText, muted, heading, link, code, codeBackground,
                tableRowBackground, quoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different muted (secondary) text colour.
     *
     * @param newMuted the replacement muted colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withMuted(DocumentColor newMuted) {
        return new ColorTokens(text, newMuted, heading, link, code, codeBackground,
                tableRowBackground, quoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different heading text colour.
     *
     * @param newHeading the replacement heading colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withHeading(DocumentColor newHeading) {
        return new ColorTokens(text, muted, newHeading, link, code, codeBackground,
                tableRowBackground, quoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different hyperlink colour.
     *
     * @param newLink the replacement link colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withLink(DocumentColor newLink) {
        return new ColorTokens(text, muted, heading, newLink, code, codeBackground,
                tableRowBackground, quoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different inline-code text colour.
     *
     * @param newCode the replacement code colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withCode(DocumentColor newCode) {
        return new ColorTokens(text, muted, heading, link, newCode, codeBackground,
                tableRowBackground, quoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different table body-row background colour.
     *
     * @param newTableRowBackground the replacement table row background
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withTableRowBackground(DocumentColor newTableRowBackground) {
        return new ColorTokens(text, muted, heading, link, code, codeBackground,
                newTableRowBackground, quoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different blockquote accent-bar colour.
     *
     * @param newQuoteBar the replacement quote-bar colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withQuoteBar(DocumentColor newQuoteBar) {
        return new ColorTokens(text, muted, heading, link, code, codeBackground,
                tableRowBackground, newQuoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different blockquote text colour.
     *
     * @param newQuoteText the replacement quote text colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withQuoteText(DocumentColor newQuoteText) {
        return new ColorTokens(text, muted, heading, link, code, codeBackground,
                tableRowBackground, quoteBar, newQuoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different rule / divider colour.
     *
     * @param newRule the replacement rule colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withRule(DocumentColor newRule) {
        return new ColorTokens(text, muted, heading, link, code, codeBackground,
                tableRowBackground, quoteBar, quoteText, newRule, accent, surface);
    }

    /**
     * Returns a copy with a different page-background (surface) colour.
     *
     * @param newSurface the replacement surface colour; {@code null} for no page fill
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withSurface(DocumentColor newSurface) {
        return new ColorTokens(text, muted, heading, link, code, codeBackground,
                tableRowBackground, quoteBar, quoteText, rule, accent, newSurface);
    }
}
