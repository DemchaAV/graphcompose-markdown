package io.github.demchaav.markdown.model.inline;

import java.util.List;
import java.util.Objects;

/**
 * A hyperlink wrapping the inline content that forms its visible label.
 *
 * <p>The {@code title} is captured for programmatic use but is not currently rendered
 * (PDF has no native link-tooltip concept).</p>
 *
 * @param url      the link destination
 * @param title    the optional link title (tooltip); may be {@code null}
 * @param children the inline content forming the visible link text
 */
public record LinkRun(String url, String title, List<InlineNode> children) implements InlineNode {

    /**
     * Creates a link run.
     *
     * @param url      the link destination; must not be {@code null}
     * @param title    the optional link title; may be {@code null}
     * @param children the visible link content; copied defensively, must not be {@code null}
     */
    public LinkRun {
        Objects.requireNonNull(url, "url");
        children = List.copyOf(Objects.requireNonNull(children, "children"));
    }
}
