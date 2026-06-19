package io.github.demchaav.markdown.model;

import java.util.List;
import java.util.Objects;

/**
 * A custom fenced block introduced by {@code :::} syntax, e.g.
 *
 * <pre>{@code
 * :::callout warning
 * This configuration is unsafe.
 * :::
 * }</pre>
 *
 * <p>The block carries a {@code type} (e.g. {@code "callout"}) and an optional
 * {@code variant} (e.g. {@code "warning"}). A theme decides how each type renders
 * by registering a {@code NodeRenderer} for {@code CustomBlockNode}; unknown
 * types fall back to a default rendering of their nested content.</p>
 *
 * @param type    the block type, lower-cased (e.g. {@code "callout"})
 * @param variant the optional variant (e.g. {@code "warning"}); may be {@code null}
 * @param content the nested block content
 */
public record CustomBlockNode(String type, String variant, List<MarkdownNode> content) implements MarkdownNode {

    /**
     * Creates a custom block node.
     *
     * @param type    the block type; must not be {@code null}
     * @param variant the optional variant; may be {@code null}
     * @param content the nested block content; copied defensively, must not be {@code null}
     */
    public CustomBlockNode {
        Objects.requireNonNull(type, "type");
        content = List.copyOf(Objects.requireNonNull(content, "content"));
    }
}
