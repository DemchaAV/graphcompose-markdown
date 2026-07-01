package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.extension.ImageResolver;
import io.github.demchaav.markdown.extension.SyntaxHighlighter;
import io.github.demchaav.markdown.model.AlertNode;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.FootnoteDefinitionNode;
import io.github.demchaav.markdown.model.FootnotesNode;
import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.model.ListItemNode;
import io.github.demchaav.markdown.model.ListNode;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.model.QuoteNode;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;

import java.util.ArrayList;
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
    private final RenderState state;

    /**
     * Creates a render context. Each context created through this constructor
     * starts a fresh document scope (heading-anchor and footnote bookkeeping);
     * {@link #withTextColor(DocumentColor)} children share that scope.
     *
     * @param theme          the active theme
     * @param styles         the style facade over the theme tokens
     * @param inlineRenderer the inline renderer
     * @param imageResolver  the image resolver
     */
    public RenderContext(MarkdownTheme theme, MarkdownStyles styles,
                         InlineRenderer inlineRenderer, ImageResolver imageResolver) {
        this(theme, styles, inlineRenderer, imageResolver, null, new RenderState());
    }

    private RenderContext(MarkdownTheme theme, MarkdownStyles styles, InlineRenderer inlineRenderer,
                          ImageResolver imageResolver, DocumentColor inheritedTextColor, RenderState state) {
        this.theme = theme;
        this.styles = styles;
        this.inlineRenderer = inlineRenderer;
        this.imageResolver = imageResolver;
        this.inheritedTextColor = inheritedTextColor;
        this.state = state;
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

    /** @return the code syntax highlighter */
    public SyntaxHighlighter highlighter() {
        return theme.highlighter();
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
        return new RenderContext(theme, styles, inlineRenderer, imageResolver, color, state);
    }

    /**
     * Returns a document-unique anchor slug for a heading, so a rendered heading
     * can declare a named PDF destination that {@code [text](#slug)} links jump to.
     * Matches GitHub's slug scheme, including {@code -1}/{@code -2} suffixes for
     * duplicate heading text.
     *
     * @param title the heading's plain text
     * @return a document-unique anchor name (never blank)
     */
    public String headingAnchor(String title) {
        return state.headingAnchor(title);
    }

    /**
     * Plans a stable slug anchor for every heading in the document and records the ordered
     * table-of-contents entries. Call this once before rendering so a {@code [TOC]} that appears
     * above its headings links to the same slugs the headings declare.
     *
     * @param document the document about to be rendered
     */
    public void planHeadings(MarkdownDocument document) {
        List<RenderState.HeadingRef> refs = new ArrayList<>();
        collectHeadings(document.blocks(), refs);
        state.planHeadings(refs);
    }

    /**
     * Collects headings depth-first, in document order. The container types recursed into here MUST
     * mirror the ones the renderers recurse into (QuoteRenderer, AlertRenderer, CustomBlock/Callout,
     * ListRenderer, FootnotesRenderer in {@code BuiltinRenderers}); if a new container node type is
     * added there, add it here too, or headings inside it drop out of the TOC and slug plan.
     */
    private void collectHeadings(List<MarkdownNode> blocks, List<RenderState.HeadingRef> out) {
        for (MarkdownNode block : blocks) {
            if (block instanceof HeadingNode heading) {
                out.add(new RenderState.HeadingRef(heading, inlineRenderer.plainText(heading.content()).strip()));
            } else if (block instanceof QuoteNode quote) {
                collectHeadings(quote.content(), out);
            } else if (block instanceof AlertNode alert) {
                collectHeadings(alert.content(), out);
            } else if (block instanceof CustomBlockNode custom) {
                collectHeadings(custom.content(), out);
            } else if (block instanceof ListNode list) {
                for (ListItemNode item : list.items()) {
                    collectHeadings(item.content(), out);
                }
            } else if (block instanceof FootnotesNode notes) {
                for (FootnoteDefinitionNode definition : notes.definitions()) {
                    collectHeadings(definition.content(), out);
                }
            }
        }
    }

    /**
     * Whether the planned document contains at least one heading that yields a PDF bookmark
     * (i.e. a heading with non-empty plain text). Meaningful after {@link #planHeadings};
     * used to decide whether opening the viewer's outline panel is worthwhile.
     *
     * @return {@code true} if at least one bookmarkable heading was planned
     */
    public boolean hasBookmarkableHeading() {
        return state.tocEntries().stream().anyMatch(entry -> !entry.text().isEmpty());
    }

    /** @return the planned slug anchor for a heading, or {@code null} if it was not planned */
    String headingSlug(HeadingNode node) {
        return state.slugFor(node);
    }

    /** @return the document's headings as ordered table-of-contents entries */
    List<TocEntry> tocEntries() {
        return state.tocEntries();
    }

    /**
     * Returns the back-reference anchor ({@code fnref-N}) to place on a block that
     * is the first to reference a footnote, or {@code null} if the block references
     * no fresh footnote. The footnote's note then links back to this anchor.
     *
     * @param content the block's inline content
     * @return the {@code fnref-N} anchor to declare, or {@code null}
     */
    public String footnoteBackAnchor(List<InlineNode> content) {
        return state.footnoteBackAnchor(content);
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
