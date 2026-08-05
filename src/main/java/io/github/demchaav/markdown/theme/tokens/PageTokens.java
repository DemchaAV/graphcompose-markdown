package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.document.api.DocumentPageSize;
import com.demcha.compose.document.style.DocumentInsets;

import java.util.Objects;

/**
 * Page geometry and pagination tokens.
 *
 * @param pageSize             the page size
 * @param margin               the page margin (top, right, bottom, left)
 * @param contentWidth         the usable content width in points (page width minus left/right margin)
 * @param keepHeadingWithNext  whether a heading is kept with the block it introduces across a page
 *                             break (a theme can set {@code false} to restore the plain flow)
 */
public record PageTokens(DocumentPageSize pageSize, DocumentInsets margin, double contentWidth,
                         boolean keepHeadingWithNext) {

    /** Validates the page size and margin are present. */
    public PageTokens {
        Objects.requireNonNull(pageSize, "pageSize");
        Objects.requireNonNull(margin, "margin");
    }

    /**
     * Creates page tokens that keep headings with their content (the default).
     *
     * @param pageSize     the page size
     * @param margin       the page margin (top, right, bottom, left)
     * @param contentWidth the usable content width in points
     */
    public PageTokens(DocumentPageSize pageSize, DocumentInsets margin, double contentWidth) {
        this(pageSize, margin, contentWidth, true);
    }
}
