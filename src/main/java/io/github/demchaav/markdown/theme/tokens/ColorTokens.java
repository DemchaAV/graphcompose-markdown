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
 * @param code           inline code text colour
 * @param codeBackground fenced code block / inline code background
 * @param quoteBar       blockquote left accent bar
 * @param quoteText      blockquote text colour
 * @param rule           horizontal rule / divider colour
 * @param accent         general accent (default callout bar, emphasis)
 * @param surface        page background; may be {@code null} for no fill
 */
public record ColorTokens(
        DocumentColor text,
        DocumentColor muted,
        DocumentColor heading,
        DocumentColor link,
        DocumentColor code,
        DocumentColor codeBackground,
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
                quoteBar, quoteText, rule, accent, surface);
    }

    /**
     * Returns a copy with a different accent colour.
     *
     * @param newAccent the replacement accent colour
     * @return a new {@code ColorTokens}
     */
    public ColorTokens withAccent(DocumentColor newAccent) {
        return new ColorTokens(text, muted, heading, link, code, codeBackground,
                quoteBar, quoteText, rule, newAccent, surface);
    }
}
