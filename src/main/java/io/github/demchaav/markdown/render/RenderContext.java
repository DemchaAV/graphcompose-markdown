package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.extension.ImageResolver;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;

import java.util.List;

/**
 * The context handed to every {@link NodeRenderer}: theme styles, the inline
 * renderer, the image resolver, and block dispatch.
 *
 * <p>A context can be derived with an inherited text colour
 * ({@link #withTextColor(DocumentColor)}) so container renderers (blockquotes,
 * callouts) recolour their nested content while still delegating to the shared
 * block renderers — that delegation is what lets components be reused rather
 * than re-implemented per container.</p>
 */
public final class RenderContext {

    private final MarkdownTheme theme;
    private final MarkdownStyles styles;
    private final InlineRenderer inlineRenderer;
    private final ImageResolver imageResolver;
    private final DocumentColor inheritedTextColor;

    /**
     * Creates a render context.
     *
     * @param theme          the active theme
     * @param styles         the style facade over the theme tokens
     * @param inlineRenderer the inline renderer
     * @param imageResolver  the image resolver
     */
    public RenderContext(MarkdownTheme theme, MarkdownStyles styles,
                         InlineRenderer inlineRenderer, ImageResolver imageResolver) {
        this(theme, styles, inlineRenderer, imageResolver, null);
    }

    private RenderContext(MarkdownTheme theme, MarkdownStyles styles, InlineRenderer inlineRenderer,
                          ImageResolver imageResolver, DocumentColor inheritedTextColor) {
        this.theme = theme;
        this.styles = styles;
        this.inlineRenderer = inlineRenderer;
        this.imageResolver = imageResolver;
        this.inheritedTextColor = inheritedTextColor;
    }

    /** @return the style facade */
    public MarkdownStyles styles() {
        return styles;
    }

    /** @return the design tokens */
    public MarkdownTokens tokens() {
        return styles.tokens();
    }

    /** @return the inline renderer */
    public InlineRenderer inline() {
        return inlineRenderer;
    }

    /** @return the image resolver */
    public ImageResolver images() {
        return imageResolver;
    }

    /** @return the base inline style for body paragraphs, honouring any inherited text colour */
    public InlineStyle paragraphInline() {
        return inheritedTextColor == null ? styles.paragraphInline() : styles.bodyInlineFor(inheritedTextColor);
    }

    /**
     * @param level the heading level
     * @return the base inline style for a heading
     */
    public InlineStyle headingInline(int level) {
        return styles.headingInline(level);
    }

    /**
     * Convenience: render inline nodes against a base style.
     *
     * @param nodes the inline nodes
     * @param base  the base style
     * @return the rich text
     */
    public RichText toRich(List<InlineNode> nodes, InlineStyle base) {
        return inlineRenderer.render(nodes, base);
    }

    /**
     * Returns a context that renders subsequent body text in the given colour.
     *
     * @param color the inherited text colour
     * @return a derived context
     */
    public RenderContext withTextColor(DocumentColor color) {
        return new RenderContext(theme, styles, inlineRenderer, imageResolver, color);
    }

    /**
     * Renders a single block into the host via its registered renderer.
     *
     * @param node the block node
     * @param host the host section
     */
    public void renderBlock(MarkdownNode node, SectionBuilder host) {
        theme.registry().render(node, host, this);
    }

    /**
     * Returns the renderer bound to a {@code :::} custom-block type, or {@code null}.
     * Used by the default custom-block dispatcher to route by type.
     *
     * @param type the custom-block type
     * @return the type-specific renderer, or {@code null} for the default
     */
    public NodeRenderer<CustomBlockNode> customBlockRenderer(String type) {
        return theme.registry().customBlockRenderer(type);
    }

    /**
     * Renders a sequence of blocks into the host, setting uniform block spacing.
     *
     * @param nodes the block nodes
     * @param host  the host section
     */
    public void renderBlocks(List<MarkdownNode> nodes, SectionBuilder host) {
        host.spacing(styles.blockSpacing());
        for (MarkdownNode node : nodes) {
            renderBlock(node, host);
        }
    }
}
