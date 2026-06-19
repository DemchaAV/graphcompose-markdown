package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.document.api.DocumentPageSize;
import com.demcha.compose.document.style.DocumentInsets;

import java.util.Objects;

/**
 * Page geometry tokens.
 *
 * @param pageSize     the page size
 * @param margin       the page margin (top, right, bottom, left)
 * @param contentWidth the usable content width in points (page width minus left/right margin)
 */
public record PageTokens(DocumentPageSize pageSize, DocumentInsets margin, double contentWidth) {

    /** Validates the page size and margin are present. */
    public PageTokens {
        Objects.requireNonNull(pageSize, "pageSize");
        Objects.requireNonNull(margin, "margin");
    }
}
