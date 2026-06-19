package io.github.demchaav.markdown.theme;

import com.demcha.compose.document.dsl.SectionBuilder;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.render.NodeRenderer;
import io.github.demchaav.markdown.render.RenderContext;

import java.util.HashMap;
import java.util.Locale;
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
    private final Map<String, NodeRenderer<CustomBlockNode>> customBlockRenderers;

    /** Creates an empty registry. */
    public RendererRegistry() {
        this.renderers = new HashMap<>();
        this.customBlockRenderers = new HashMap<>();
    }

    /**
     * Creates a registry copied from another.
     *
     * @param source the registry to copy bindings from
     */
    public RendererRegistry(RendererRegistry source) {
        Objects.requireNonNull(source, "source");
        this.renderers = new HashMap<>(source.renderers);
        this.customBlockRenderers = new HashMap<>(source.customBlockRenderers);
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
     * Binds a renderer to a specific {@code :::} custom-block type (e.g. {@code "chart"}),
     * replacing any existing binding. The default custom-block dispatcher routes blocks of
     * that type to this renderer; unbound types fall back to the callout rendering. This is
     * the seam for project-specific block types.
     *
     * @param type     the custom-block type (matched case-insensitively)
     * @param renderer the renderer for that type
     * @return this registry, for chaining
     */
    public RendererRegistry registerCustomBlock(String type, NodeRenderer<CustomBlockNode> renderer) {
        customBlockRenderers.put(
                Objects.requireNonNull(type, "type").toLowerCase(Locale.ROOT),
                Objects.requireNonNull(renderer, "renderer"));
        return this;
    }

    /**
     * Returns the renderer bound to a custom-block type, or {@code null} if none is bound.
     *
     * @param type the custom-block type
     * @return the renderer, or {@code null}
     */
    public NodeRenderer<CustomBlockNode> customBlockRenderer(String type) {
        return type == null ? null : customBlockRenderers.get(type.toLowerCase(Locale.ROOT));
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
