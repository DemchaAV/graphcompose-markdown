package io.github.demchaav.markdown.model;

/**
 * A block-level element of a {@link MarkdownDocument}.
 *
 * <p>Block nodes are the units a theme's renderers are keyed on: each concrete
 * type maps to a {@code NodeRenderer} that emits GraphCompose builders. The
 * hierarchy is sealed so renderers and tests can exhaustively switch over it.
 * The model is independent of GraphCompose — it carries only content and
 * structure, never styling or layout.</p>
 */
public sealed interface MarkdownNode
        permits HeadingNode, ParagraphNode, ListNode, CodeBlockNode, QuoteNode,
                ThematicBreakNode, ImageNode, TableNode, CustomBlockNode, FootnotesNode,
                AlertNode, FrontMatterNode, TocNode, UnsupportedBlockNode {
}
