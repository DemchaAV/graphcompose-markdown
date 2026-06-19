package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.SectionBuilder;
import io.github.demchaav.markdown.model.MarkdownNode;

/**
 * Renders one kind of {@link MarkdownNode} into a GraphCompose section — layer
 * three of the theme model (engine behaviour).
 *
 * <p>A renderer decides how a node becomes GraphCompose builders: spacing,
 * container shape, list markers, child layout. It reads its styling from the
 * {@link RenderContext} (theme tokens and component styles), never from inline
 * literals. Renderers are stateless and are bound to node types in a
 * {@code RendererRegistry}; overriding one renderer is how a theme changes the
 * behaviour for a single element while reusing every other component.</p>
 *
 * @param <N> the node type this renderer handles
 */
@FunctionalInterface
public interface NodeRenderer<N extends MarkdownNode> {

    /**
     * Renders a node into the host section.
     *
     * @param node the node to render
     * @param host the section to add content to
     * @param ctx  the render context (theme, styles, inline renderer, image resolver)
     */
    void render(N node, SectionBuilder host, RenderContext ctx);
}
