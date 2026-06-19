package io.github.demchaav.markdown.theme;

import io.github.demchaav.markdown.extension.DefaultImageResolver;
import io.github.demchaav.markdown.extension.ImageResolver;
import io.github.demchaav.markdown.extension.RegexSyntaxHighlighter;
import io.github.demchaav.markdown.extension.SyntaxHighlighter;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.render.NodeRenderer;
import io.github.demchaav.markdown.render.RendererPack;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;

import java.util.Objects;

/**
 * A theme: design tokens, the renderer registry, and the image resolver.
 *
 * <p>Build a theme from scratch with {@link #builder()} (then register tokens
 * and renderers), or — more usually — derive one from an existing theme with
 * {@link #builder(MarkdownTheme)}, which copies its tokens, renderers and image
 * resolver so you override only what differs. That copy-and-override is how
 * components are reused across themes.</p>
 */
public final class MarkdownTheme {

    private final MarkdownTokens tokens;
    private final RendererRegistry registry;
    private final ImageResolver imageResolver;
    private final SyntaxHighlighter highlighter;

    private MarkdownTheme(Builder builder) {
        this.tokens = Objects.requireNonNull(builder.tokens, "tokens");
        this.registry = builder.registry;
        this.imageResolver = builder.imageResolver;
        this.highlighter = builder.highlighter;
    }

    /** @return the design tokens */
    public MarkdownTokens tokens() {
        return tokens;
    }

    /** @return the renderer registry */
    public RendererRegistry registry() {
        return registry;
    }

    /** @return the image resolver */
    public ImageResolver imageResolver() {
        return imageResolver;
    }

    /** @return the code syntax highlighter */
    public SyntaxHighlighter highlighter() {
        return highlighter;
    }

    /**
     * Starts an empty theme builder (no tokens, no renderers).
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Starts a theme builder seeded from an existing theme — copies its tokens,
     * renderers and image resolver.
     *
     * @param base the theme to copy
     * @return a new builder
     */
    public static Builder builder(MarkdownTheme base) {
        return new Builder(base);
    }

    /**
     * Returns a builder seeded from this theme.
     *
     * @return a new builder
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /** Builder for {@link MarkdownTheme}. */
    public static final class Builder {

        private MarkdownTokens tokens;
        private final RendererRegistry registry;
        private ImageResolver imageResolver;
        private SyntaxHighlighter highlighter;

        private Builder() {
            this.registry = new RendererRegistry();
            this.imageResolver = new DefaultImageResolver();
            this.highlighter = new RegexSyntaxHighlighter();
        }

        private Builder(MarkdownTheme base) {
            Objects.requireNonNull(base, "base");
            this.tokens = base.tokens;
            this.registry = new RendererRegistry(base.registry);
            this.imageResolver = base.imageResolver;
            this.highlighter = base.highlighter;
        }

        /**
         * Sets the design tokens.
         *
         * @param newTokens the tokens
         * @return this builder
         */
        public Builder tokens(MarkdownTokens newTokens) {
            this.tokens = Objects.requireNonNull(newTokens, "tokens");
            return this;
        }

        /**
         * Overrides the renderer for a node type.
         *
         * @param type     the node type
         * @param renderer the renderer
         * @param <N>      the node type
         * @return this builder
         */
        public <N extends MarkdownNode> Builder renderer(Class<N> type, NodeRenderer<N> renderer) {
            registry.register(type, renderer);
            return this;
        }

        /**
         * Applies a renderer pack — registers all of its renderers, overriding earlier
         * bindings for the same node types. Compose packs from several sources.
         *
         * @param pack the renderer pack
         * @return this builder
         */
        public Builder pack(RendererPack pack) {
            Objects.requireNonNull(pack, "pack").registerInto(registry);
            return this;
        }

        /**
         * Binds a renderer to a specific {@code :::} custom-block type (e.g. {@code "chart"}),
         * so a project can render its own block types while reusing every other renderer.
         *
         * @param type     the custom-block type (case-insensitive)
         * @param renderer the renderer for that type
         * @return this builder
         */
        public Builder customBlock(String type, NodeRenderer<CustomBlockNode> renderer) {
            registry.registerCustomBlock(type, renderer);
            return this;
        }

        /** @return the registry being built, for bulk registration of defaults */
        public RendererRegistry registry() {
            return registry;
        }

        /**
         * Sets the image resolver.
         *
         * @param resolver the image resolver
         * @return this builder
         */
        public Builder imageResolver(ImageResolver resolver) {
            this.imageResolver = Objects.requireNonNull(resolver, "resolver");
            return this;
        }

        /**
         * Sets the code syntax highlighter (default {@link RegexSyntaxHighlighter}).
         *
         * @param newHighlighter the highlighter
         * @return this builder
         */
        public Builder highlighter(SyntaxHighlighter newHighlighter) {
            this.highlighter = Objects.requireNonNull(newHighlighter, "highlighter");
            return this;
        }

        /**
         * Builds the theme.
         *
         * @return a new {@link MarkdownTheme}
         */
        public MarkdownTheme build() {
            return new MarkdownTheme(this);
        }
    }
}
