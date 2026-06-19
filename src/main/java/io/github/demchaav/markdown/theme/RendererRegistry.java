package io.github.demchaav.markdown.theme;

import com.demcha.compose.document.dsl.SectionBuilder;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.render.NodeRenderer;
import io.github.demchaav.markdown.render.RenderContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Binds {@link MarkdownNode} types to their {@link NodeRenderer}s.
 *
 * <p>A theme owns one registry. Composing a theme from another copies the
 * registry, after which individual bindings can be overridden — this is how
 * renderers are reused across themes. Lookup is by exact runtime class.</p>
 */
public final class RendererRegistry {

    private final Map<Class<? extends MarkdownNode>, NodeRenderer<? extends MarkdownNode>> renderers;

    /** Creates an empty registry. */
    public RendererRegistry() {
        this.renderers = new HashMap<>();
    }

    /**
     * Creates a registry copied from another.
     *
     * @param source the registry to copy bindings from
     */
    public RendererRegistry(RendererRegistry source) {
        this.renderers = new HashMap<>(Objects.requireNonNull(source, "source").renderers);
    }

    /**
     * Binds a renderer to a node type, replacing any existing binding.
     *
     * @param type     the node type
     * @param renderer the renderer for that type
     * @param <N>      the node type
     * @return this registry, for chaining
     */
    public <N extends MarkdownNode> RendererRegistry register(Class<N> type, NodeRenderer<N> renderer) {
        renderers.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(renderer, "renderer"));
        return this;
    }

    /**
     * Returns whether a renderer is bound for the given node type.
     *
     * @param type the node type
     * @return {@code true} if a renderer is registered
     */
    public boolean hasRenderer(Class<? extends MarkdownNode> type) {
        return renderers.containsKey(type);
    }

    /**
     * Dispatches a node to its registered renderer. Nodes with no registered
     * renderer are skipped.
     *
     * @param node the node to render
     * @param host the section to render into
     * @param ctx  the render context
     */
    @SuppressWarnings("unchecked")
    public void render(MarkdownNode node, SectionBuilder host, RenderContext ctx) {
        NodeRenderer<MarkdownNode> renderer =
                (NodeRenderer<MarkdownNode>) renderers.get(node.getClass());
        if (renderer != null) {
            renderer.render(node, host, ctx);
        }
    }
}
