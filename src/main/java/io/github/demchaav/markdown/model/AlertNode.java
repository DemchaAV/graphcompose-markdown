package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * A GitHub-style alert — a blockquote whose first line is an alert marker
 * (`> [!NOTE]`, `> [!WARNING]`, …). The marker is consumed; {@code content} is the
 * remaining block content, rendered inside a titled, colour-coded callout.
 *
 * @param type    the alert kind
 * @param content the block content after the marker line
 */
public record AlertNode(AlertType type, List<MarkdownNode> content) implements MarkdownNode {

    /**
     * Creates an alert node.
     *
     * @param type    the alert kind; must not be {@code null}
     * @param content the block content; copied defensively, must not be {@code null}
     */
    public AlertNode {
        Objects.requireNonNull(type, "type");
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }
}
