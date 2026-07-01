package io.github.demchaav.markdown.render;

import java.util.Locale;

/**
 * GitHub-compatible heading slugs, used to turn a heading's text into a stable
 * anchor name and to resolve {@code [text](#fragment)} links against it.
 *
 * <p>The rule mirrors GitHub's heading anchors: lower-case the text, drop every
 * character that is not a letter, digit, hyphen or underscore, and collapse runs
 * of whitespace into a single hyphen. So {@code "## My Heading!"} and a link to
 * {@code "#My Heading"} both resolve to {@code my-heading}. Per-document
 * de-duplication (appending {@code -1}, {@code -2}, …) lives in
 * {@link RenderState}, since it depends on document order.</p>
 */
final class Slugs {

    private Slugs() {
    }

    /**
     * Slugifies a fragment of text the way GitHub anchors headings.
     *
     * @param text the source text (a heading's plain text, or a link fragment)
     * @return the slug, possibly empty if the text had no slug-able characters
     */
    static String slugify(String text) {
        if (text == null) {
            return "";
        }
        String lower = text.strip().toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(lower.length());
        boolean pendingSeparator = false;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                if (pendingSeparator && out.length() > 0) {
                    out.append('-');
                }
                pendingSeparator = false;
                out.append(c);
            } else if (Character.isWhitespace(c)) {
                // Defer the hyphen so trailing whitespace does not leave a dangling separator.
                pendingSeparator = true;
            }
            // Any other punctuation is dropped, matching GitHub's `[^\w\s-]` strip.
        }
        return out.toString();
    }
}
