package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.dsl.SectionBuilder;
import com.demcha.compose.document.image.DocumentImageFitMode;
import com.demcha.compose.document.node.DocumentBookmarkOptions;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentCornerRadius;
import com.demcha.compose.document.style.DocumentInsets;
import com.demcha.compose.document.style.DocumentTextDecoration;
import com.demcha.compose.document.style.DocumentTextStyle;
import io.github.demchaav.markdown.extension.CodeToken;
import io.github.demchaav.markdown.model.AlertNode;
import io.github.demchaav.markdown.model.AlertType;
import io.github.demchaav.markdown.model.CodeBlockNode;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.FootnotesNode;
import io.github.demchaav.markdown.model.FrontMatterNode;
import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.model.ImageNode;
import io.github.demchaav.markdown.model.ListNode;
import io.github.demchaav.markdown.model.UnsupportedBlockNode;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.QuoteNode;
import io.github.demchaav.markdown.model.TableNode;
import io.github.demchaav.markdown.model.ThematicBreakNode;
import io.github.demchaav.markdown.model.TocNode;
import io.github.demchaav.markdown.theme.RendererRegistry;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import io.github.demchaav.markdown.theme.tokens.AlertColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The default {@link NodeRenderer} registration plus the smaller built-in renderers.
 * The larger ones live in their own files in this package — {@link ListRenderer},
 * {@link TableRenderer}, {@link FootnotesRenderer} and {@link TocRenderer}.
 *
 * <p>Each renderer adds exactly one child to the host section so the host's
 * uniform block spacing applies cleanly between blocks. Panels (code blocks,
 * callouts) and accented asides (blockquotes) are nested sections that grow with
 * their content, so they paginate correctly.</p>
 */
public final class BuiltinRenderers {

    private BuiltinRenderers() {
    }

    /**
     * Registers the default renderers for all built-in node types.
     *
     * @param registry the registry to populate
     */
    public static void registerDefaults(RendererRegistry registry) {
        registry.register(HeadingNode.class, new HeadingRenderer());
        registry.register(ParagraphNode.class, new ParagraphRenderer());
        registry.register(ListNode.class, new ListRenderer());
        registry.register(CodeBlockNode.class, new CodeBlockRenderer());
        registry.register(QuoteNode.class, new QuoteRenderer());
        registry.register(ThematicBreakNode.class, new ThematicBreakRenderer());
        registry.register(ImageNode.class, new ImageRenderer());
        registry.register(TableNode.class, new TableRenderer());
        registry.register(CustomBlockNode.class, new CustomBlockDispatchRenderer());
        registry.register(FootnotesNode.class, new FootnotesRenderer());
        registry.register(UnsupportedBlockNode.class, new UnsupportedBlockRenderer());
        registry.register(AlertNode.class, new AlertRenderer());
        registry.register(FrontMatterNode.class, new FrontMatterRenderer());
        registry.register(TocNode.class, new TocRenderer());
    }

    /**
     * Renders a heading as a styled paragraph with space above it, attaches a PDF
     * bookmark (outline entry) at its level so the rendered document gets a navigable
     * heading tree in the viewer's outline pane, and declares a GitHub-style anchor so
     * {@code [text](#heading)} links can jump to it.
     */
    public static final class HeadingRenderer implements NodeRenderer<HeadingNode> {
        @Override
        public void render(HeadingNode node, SectionBuilder host, RenderContext ctx) {
            InlineStyle base = ctx.headingInline(node.level());
            RichText rich = ctx.toRich(node.content(), base);
            double above = ctx.styles().headingSpaceAbove(node.level());
            String title = ctx.inline().plainText(node.content()).strip();
            // Use the slug planned up front (so a [TOC] above the headings links to the same anchor),
            // falling back to an on-the-fly slug if this heading was somehow not planned.
            String planned = ctx.headingSlug(node);
            String anchor = planned != null ? planned : ctx.headingAnchor(title);
            host.addParagraph(p -> {
                p.rich(rich).margin(new DocumentInsets(above, 0, 0, 0)).anchor(anchor);
                if (!title.isEmpty()) {
                    p.bookmark(new DocumentBookmarkOptions(title, node.level()));
                }
            });
        }
    }

    /** Renders a paragraph of inline content. */
    public static final class ParagraphRenderer implements NodeRenderer<ParagraphNode> {
        @Override
        public void render(ParagraphNode node, SectionBuilder host, RenderContext ctx) {
            if (node.content().isEmpty()) {
                return;
            }
            InlineStyle base = ctx.paragraphInline();
            RichText rich = ctx.toRich(node.content(), base);
            double lineSpacing = ctx.styles().lineLeading(ctx.tokens().typography().bodySize());
            // If this paragraph is the first to cite a footnote, anchor it so the note can link back.
            String backAnchor = ctx.footnoteBackAnchor(node.content());
            host.addParagraph(p -> {
                p.rich(rich).lineSpacing(lineSpacing);
                if (backAnchor != null) {
                    p.anchor(backAnchor);
                }
            });
        }
    }

    /**
     * Renders a fenced/indented code block as a rounded, padded panel — one paragraph per
     * line, with syntax highlighting (the theme's {@code SyntaxHighlighter}) painting each
     * token. Whitespace is kept exactly: spaces become non-breaking so indentation survives
     * and long lines do not reflow mid-token.
     */
    public static final class CodeBlockRenderer implements NodeRenderer<CodeBlockNode> {
        @Override
        public void render(CodeBlockNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.CodeBlockStyle style = ctx.styles().codeBlock();
            List<CodeToken> tokens = ctx.highlighter().highlight(node.code(), node.language());
            List<RichText> lines = toColouredLines(tokens, style, ctx);
            DocumentTextStyle plainStyle = codeTokenStyle(style, style.textColor());
            host.addSection(panel -> {
                panel.softPanel(style.background(), style.cornerRadius(), style.padding());
                panel.keepTogether();
                panel.spacing(1);
                for (RichText line : lines) {
                    if (line == null) {
                        panel.addParagraph(p -> p.text(" ").textStyle(plainStyle).lineSpacing(style.lineSpacing()));
                    } else {
                        panel.addParagraph(p -> p.rich(line).lineSpacing(style.lineSpacing()));
                    }
                }
            });
        }

        /** Splits the token stream into per-line rich text; a {@code null} entry is a blank line. */
        private static List<RichText> toColouredLines(List<CodeToken> tokens,
                                                      MarkdownStyles.CodeBlockStyle style, RenderContext ctx) {
            List<RichText> lines = new ArrayList<>();
            RichText line = RichText.empty();
            boolean hasContent = false;
            for (CodeToken token : tokens) {
                DocumentTextStyle tokenStyle = codeTokenStyle(style, ctx.styles().syntaxColor(token.type()));
                String[] parts = token.text().split("\n", -1);
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) {
                        lines.add(hasContent ? line : null);
                        line = RichText.empty();
                        hasContent = false;
                    }
                    String piece = preserveWhitespace(parts[i]);
                    if (!piece.isEmpty()) {
                        // Geometric emoji (🔴 🟢 …) have no glyph in any PDF font and would render as
                        // "?" even inside code; draw them as vector shapes (the rest stays verbatim).
                        if (EmojiShapes.contains(piece)) {
                            EmojiShapes.append(line, piece, tokenStyle, style.size());
                        } else {
                            line.style(piece, tokenStyle);
                        }
                        hasContent = true;
                    }
                }
            }
            lines.add(hasContent ? line : null);
            return lines;
        }

        private static DocumentTextStyle codeTokenStyle(MarkdownStyles.CodeBlockStyle style, DocumentColor color) {
            return DocumentTextStyle.builder()
                    .fontName(style.family().resolve(false, false))
                    .size(style.size())
                    .color(color)
                    .decoration(DocumentTextDecoration.DEFAULT)
                    .build();
        }

        /** Non-breaking spaces keep indentation and prevent mid-line reflow in the monospace panel. */
        private static String preserveWhitespace(String text) {
            return text.replace("\t", "    ").replace(" ", " ");
        }
    }

    /**
     * Renders an unsupported block's raw source as muted monospace text. The default,
     * lenient behaviour: surface content the mapper could not model rather than lose it.
     */
    public static final class UnsupportedBlockRenderer implements NodeRenderer<UnsupportedBlockNode> {
        @Override
        public void render(UnsupportedBlockNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.CodeBlockStyle style = ctx.styles().codeBlock();
            DocumentTextStyle rawStyle = DocumentTextStyle.builder()
                    .fontName(style.family().resolve(false, false))
                    .size(style.size())
                    .color(ctx.tokens().colors().muted())
                    .decoration(DocumentTextDecoration.DEFAULT)
                    .build();
            // stripTrailing drops the source's trailing newline so it does not render as a
            // spurious blank line; render in a tight panel (like code) so a multi-line raw
            // block reads as one cohesive, page-break-safe chunk rather than loose paragraphs.
            String raw = node.raw().stripTrailing();
            String[] lines = raw.isEmpty() ? new String[]{" "} : raw.split("\n", -1);
            host.addSection(panel -> {
                panel.softPanel(style.background(), style.cornerRadius(), style.padding());
                panel.keepTogether();
                panel.spacing(1);
                for (String line : lines) {
                    String content = line.isEmpty() ? " " : line;
                    panel.addParagraph(p -> p.text(content).textStyle(rawStyle).lineSpacing(style.lineSpacing()));
                }
            });
        }
    }

    /** Renders a blockquote as a left-accented aside whose children are recoloured. */
    public static final class QuoteRenderer implements NodeRenderer<QuoteNode> {
        @Override
        public void render(QuoteNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.QuoteStyle style = ctx.styles().quote();
            RenderContext childCtx = ctx.withTextColor(style.textColor());
            double padding = style.padding();
            host.addSection(quote -> {
                quote.fillColor(style.background());
                // Round only the right corners so the plate sits flush against the left accent bar.
                quote.cornerRadius(DocumentCornerRadius.right(style.cornerRadius()));
                quote.accentLeft(style.barColor(), style.barWidth());
                quote.padding(new DocumentInsets(padding, padding, padding, padding + style.barWidth()));
                quote.keepTogether();
                childCtx.renderBlocks(node.content(), quote);
            });
        }
    }

    /** Renders a thematic break as a horizontal rule. */
    public static final class ThematicBreakRenderer implements NodeRenderer<ThematicBreakNode> {
        @Override
        public void render(ThematicBreakNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.RuleStyle style = ctx.styles().rule();
            host.addLine(line -> line.horizontal(style.width()).color(style.color()).thickness(style.thickness()));
        }
    }

    /** Renders an image fitted to the content width, falling back to muted alt text. */
    public static final class ImageRenderer implements NodeRenderer<ImageNode> {
        @Override
        public void render(ImageNode node, SectionBuilder host, RenderContext ctx) {
            double width = ctx.tokens().page().contentWidth();
            Optional<byte[]> bytes = ctx.images().resolve(node.source());
            if (bytes.isPresent()) {
                byte[] data = bytes.get();
                host.addImage(img -> img.source(data).fitToBounds(width, width).fitMode(DocumentImageFitMode.CONTAIN));
            } else {
                String alt = node.alt().isEmpty() ? node.source() : node.alt();
                DocumentTextStyle muted = DocumentTextStyle.builder()
                        .fontName(ctx.tokens().typography().bodyFamily().resolve(false, true))
                        .size(ctx.tokens().typography().bodySize())
                        .color(ctx.tokens().colors().muted())
                        .decoration(DocumentTextDecoration.DEFAULT)
                        .build();
                host.addParagraph(p -> p.text("[" + alt + "]").textStyle(muted));
            }
        }
    }

    /**
     * Default {@code :::} custom-block renderer: routes a block to the renderer registered
     * for its {@code type} (via {@code registry.registerCustomBlock(...)}), falling back to
     * the callout style for unbound types. This is the per-type extension seam.
     */
    public static final class CustomBlockDispatchRenderer implements NodeRenderer<CustomBlockNode> {
        private final NodeRenderer<CustomBlockNode> fallback = new CalloutRenderer();

        @Override
        public void render(CustomBlockNode node, SectionBuilder host, RenderContext ctx) {
            NodeRenderer<CustomBlockNode> renderer = ctx.customBlockRenderer(node.type());
            (renderer != null ? renderer : fallback).render(node, host, ctx);
        }
    }

    /**
     * Renders YAML front matter as a title block: the {@code title}, an italic
     * {@code subtitle}, and an {@code author} · {@code date} line, above a divider rule.
     * Unknown keys are ignored (they remain on the node for programmatic use).
     */
    public static final class FrontMatterRenderer implements NodeRenderer<FrontMatterNode> {
        @Override
        public void render(FrontMatterNode node, SectionBuilder host, RenderContext ctx) {
            String title = node.first("title");
            String subtitle = node.first("subtitle");
            String meta = joinMeta(node.first("author"), node.first("date"));
            if (title == null && subtitle == null && meta.isEmpty()) {
                return; // metadata only — nothing to render
            }

            var typo = ctx.tokens().typography();
            var colors = ctx.tokens().colors();

            if (title != null) {
                DocumentTextStyle titleStyle = DocumentTextStyle.builder()
                        .fontName(typo.headingFamily().resolve(true, false))
                        .size(typo.headingSize(1) * 1.25)
                        .color(colors.heading())
                        .decoration(DocumentTextDecoration.DEFAULT)
                        .build();
                host.addParagraph(p -> p.text(title).textStyle(titleStyle));
            }
            if (subtitle != null) {
                DocumentTextStyle subtitleStyle = DocumentTextStyle.builder()
                        .fontName(typo.bodyFamily().resolve(false, true))
                        .size(typo.bodySize() * 1.15)
                        .color(colors.muted())
                        .decoration(DocumentTextDecoration.DEFAULT)
                        .build();
                host.addParagraph(p -> p.text(subtitle).textStyle(subtitleStyle)
                        .margin(new DocumentInsets(2, 0, 0, 0)));
            }
            if (!meta.isEmpty()) {
                DocumentTextStyle metaStyle = DocumentTextStyle.builder()
                        .fontName(typo.bodyFamily().resolve(false, false))
                        .size(typo.bodySize())
                        .color(colors.muted())
                        .decoration(DocumentTextDecoration.DEFAULT)
                        .build();
                host.addParagraph(p -> p.text(meta).textStyle(metaStyle)
                        .margin(new DocumentInsets(4, 0, 0, 0)));
            }
            MarkdownStyles.RuleStyle rule = ctx.styles().rule();
            host.addLine(line -> line.horizontal(rule.width()).color(rule.color()).thickness(rule.thickness()));
        }

        private static String joinMeta(String author, String date) {
            if (author != null && date != null) {
                return author + "  ·  " + date;
            }
            return author != null ? author : (date != null ? date : "");
        }
    }

    /**
     * Renders a GitHub-style alert (`> [!NOTE]`, …) as a colour-coded, left-accented
     * callout with a bold title line in the alert's colour.
     */
    public static final class AlertRenderer implements NodeRenderer<AlertNode> {
        /** Icon height relative to the title's body size, so the glyph reads a touch taller than the caps. */
        private static final double ICON_SIZE_FACTOR = 1.15;

        @Override
        public void render(AlertNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.CalloutStyle style = ctx.styles().callout();
            DocumentColor accent = accentFor(node.type(), ctx.tokens().alertColors());
            DocumentColor background = accent.withOpacity(0.12);
            DocumentTextStyle titleStyle = DocumentTextStyle.builder()
                    .fontName(ctx.tokens().typography().bodyFamily().resolve(true, false))
                    .size(ctx.tokens().typography().bodySize())
                    .color(accent)
                    .decoration(DocumentTextDecoration.DEFAULT)
                    .build();
            double iconSize = ctx.tokens().typography().bodySize() * ICON_SIZE_FACTOR;
            host.addSection(panel -> {
                // Round only the right corners — the left edge meets the accent bar.
                panel.softPanel(background, DocumentCornerRadius.right(style.cornerRadius()), style.padding());
                panel.accentLeft(accent, style.accentWidth());
                panel.keepTogether();
                panel.addParagraph(p -> p.rich(rich -> {
                    if (AlertIcons.append(rich, node.type(), accent, iconSize)) {
                        rich.plain("  ");
                    }
                    rich.style(node.type().title(), titleStyle);
                }));
                ctx.renderBlocks(node.content(), panel);
            });
        }

        private static DocumentColor accentFor(AlertType type, AlertColors colors) {
            switch (type) {
                case TIP:
                    return colors.tip();
                case IMPORTANT:
                    return colors.important();
                case WARNING:
                    return colors.warning();
                case CAUTION:
                    return colors.caution();
                case NOTE:
                default:
                    return colors.note();
            }
        }
    }

    /** Renders a {@code :::} custom block as a tinted, left-accented callout. */
    public static final class CalloutRenderer implements NodeRenderer<CustomBlockNode> {
        @Override
        public void render(CustomBlockNode node, SectionBuilder host, RenderContext ctx) {
            MarkdownStyles.CalloutStyle style = ctx.styles().callout();
            DocumentColor accent = accentFor(node.variant(), ctx.tokens().alertColors(), style.accent());
            DocumentColor background = accent.withOpacity(0.12);
            host.addSection(panel -> {
                // Round only the right corners — the left edge meets the accent bar.
                panel.softPanel(background, DocumentCornerRadius.right(style.cornerRadius()), style.padding());
                panel.accentLeft(accent, style.accentWidth());
                panel.keepTogether();
                ctx.renderBlocks(node.content(), panel);
            });
        }

        private static DocumentColor accentFor(String variant, AlertColors colors, DocumentColor fallback) {
            if (variant == null) {
                return fallback;
            }
            switch (variant.toLowerCase()) {
                case "warning":
                case "caution":
                    return colors.calloutWarning();
                case "info":
                case "note":
                    return colors.calloutInfo();
                case "error":
                case "danger":
                    return colors.calloutError();
                case "success":
                case "tip":
                    return colors.calloutSuccess();
                default:
                    return fallback;
            }
        }
    }
}
