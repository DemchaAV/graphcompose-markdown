package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.image.DocumentImageData;
import com.demcha.compose.document.node.DocumentLinkOptions;
import com.demcha.compose.document.node.InlineImageAlignment;
import com.demcha.compose.font.FontName;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentTextDecoration;
import com.demcha.compose.document.style.DocumentTextStyle;
import io.github.demchaav.markdown.extension.EmojiResolver;
import io.github.demchaav.markdown.model.inline.CodeRun;
import io.github.demchaav.markdown.model.inline.EmojiRun;
import io.github.demchaav.markdown.model.inline.EmphasisRun;
import io.github.demchaav.markdown.model.inline.FootnoteRefRun;
import io.github.demchaav.markdown.model.inline.ImageRun;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.model.inline.LineBreakRun;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.model.inline.StrikethroughRun;
import io.github.demchaav.markdown.model.inline.StrongRun;
import io.github.demchaav.markdown.model.inline.TextRun;
import io.github.demchaav.markdown.model.inline.UnsupportedInlineRun;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.tokens.FontFamily;

import java.util.List;
import java.util.Optional;

/**
 * Flattens a tree of {@link InlineNode}s into a GraphCompose {@link RichText}.
 *
 * <p>Bold and italic are accumulated down the tree and applied via the font
 * variant; strikethrough uses the decoration enum, so the two compose freely.
 * Inline code switches to the code font and colour. Links are emitted via
 * {@link RichText#link(String, String)}; formatting inside a link degrades to
 * the link style. Emoji shortcodes resolve to inline images via the theme's
 * {@link EmojiResolver}, or fall back to literal {@code :shortcode:} text.</p>
 */
public final class InlineRenderer {

    private final EmojiResolver emojiResolver;

    /** Creates an inline renderer with no emoji image resolver (shortcodes stay text). */
    public InlineRenderer() {
        this(EmojiResolver.none());
    }

    /**
     * Creates an inline renderer with an emoji resolver.
     *
     * @param emojiResolver resolves emoji shortcodes to inline images
     */
    public InlineRenderer(EmojiResolver emojiResolver) {
        this.emojiResolver = emojiResolver == null ? EmojiResolver.none() : emojiResolver;
    }

    /**
     * Renders inline nodes into a new rich-text run list.
     *
     * @param nodes the inline nodes
     * @param base  the base style (font, size, colour) to decorate from
     * @return a populated {@link RichText}
     */
    public RichText render(List<InlineNode> nodes, InlineStyle base) {
        RichText rich = RichText.empty();
        appendInto(rich, nodes, base);
        return rich;
    }

    /**
     * Appends inline nodes into an existing rich-text builder (used to prefix a
     * list marker before the item content).
     *
     * @param rich  the builder to append into
     * @param nodes the inline nodes
     * @param base  the base style to decorate from
     */
    public void appendInto(RichText rich, List<InlineNode> nodes, InlineStyle base) {
        appendAll(rich, nodes, base, new Decor(base.baseBold(), base.baseItalic(), false, null));
    }

    /**
     * Flattens inline nodes to their plain text, dropping all styling — e.g. for a PDF
     * bookmark title or a table-of-contents entry.
     *
     * @param nodes the inline nodes
     * @return the concatenated plain text
     */
    public String plainText(List<InlineNode> nodes) {
        StringBuilder out = new StringBuilder();
        appendPlain(out, nodes);
        return out.toString();
    }

    private void appendPlain(StringBuilder out, List<InlineNode> nodes) {
        for (InlineNode node : nodes) {
            if (node instanceof TextRun text) {
                out.append(text.text());
            } else if (node instanceof CodeRun code) {
                out.append(code.text());
            } else if (node instanceof StrongRun strong) {
                appendPlain(out, strong.children());
            } else if (node instanceof EmphasisRun emphasis) {
                appendPlain(out, emphasis.children());
            } else if (node instanceof StrikethroughRun strike) {
                appendPlain(out, strike.children());
            } else if (node instanceof LinkRun link) {
                appendPlain(out, link.children().isEmpty()
                        ? List.of(new TextRun(link.url())) : link.children());
            } else if (node instanceof ImageRun image) {
                out.append(image.alt());
            } else if (node instanceof EmojiRun emoji) {
                out.append(':').append(emoji.shortcode()).append(':');
            } else if (node instanceof UnsupportedInlineRun unsupported) {
                out.append(unsupported.raw());
            } else if (node instanceof LineBreakRun) {
                out.append(' ');
            }
            // FootnoteRefRun is intentionally omitted — a [N] marker is noise in a title.
        }
    }

    private void appendAll(RichText rich, List<InlineNode> nodes, InlineStyle base, Decor decor) {
        for (InlineNode node : nodes) {
            append(rich, node, base, decor);
        }
    }

    private void append(RichText rich, InlineNode node, InlineStyle base, Decor decor) {
        if (node instanceof TextRun text) {
            emit(rich, text.text(), base, decor, false);
        } else if (node instanceof CodeRun code) {
            emit(rich, code.text(), base, decor, true);
        } else if (node instanceof StrongRun strong) {
            appendAll(rich, strong.children(), base, decor.withBold());
        } else if (node instanceof EmphasisRun emphasis) {
            appendAll(rich, emphasis.children(), base, decor.withItalic());
        } else if (node instanceof StrikethroughRun strike) {
            appendAll(rich, strike.children(), base, decor.withStrike());
        } else if (node instanceof LinkRun link) {
            List<InlineNode> children = link.children().isEmpty()
                    ? List.of(new TextRun(link.url()))
                    : link.children();
            appendAll(rich, children, base, decor.withLink(link.url()));
        } else if (node instanceof ImageRun image) {
            if (!image.alt().isEmpty()) {
                emit(rich, image.alt(), base, decor, false);
            }
        } else if (node instanceof FootnoteRefRun ref) {
            emitFootnoteRef(rich, ref, base);
        } else if (node instanceof LineBreakRun lineBreak) {
            if (lineBreak.hard()) {
                rich.plain("\n");
            } else {
                rich.space();
            }
        } else if (node instanceof EmojiRun emoji) {
            Optional<byte[]> image = emojiResolver.resolve(emoji.shortcode());
            if (image.isPresent() && image.get().length > 0) {
                double size = base.size();
                rich.image(DocumentImageData.fromBytes(image.get()), size, size,
                        InlineImageAlignment.CENTER, 0.0, linkOptionsOrNull(decor.linkUrl()));
            } else {
                // No image (or none resolved) — render the readable shortcode, never a broken glyph.
                emit(rich, ":" + emoji.shortcode() + ":", base, decor, false);
            }
        } else if (node instanceof UnsupportedInlineRun unsupported) {
            // Surface unmodelled inline (e.g. raw HTML) literally, rather than dropping it.
            emit(rich, unsupported.raw(), base, decor, false);
        }
    }

    private void emit(RichText rich, String text, InlineStyle base, Decor decor, boolean code) {
        if (text.isEmpty()) {
            return;
        }
        FontFamily family = code ? base.codeFamily() : base.family();
        double size = code ? base.codeSize() : base.size();
        FontName fontName = family.resolve(decor.bold(), decor.italic());

        if (decor.linkUrl() != null) {
            DocumentTextDecoration decoration = base.underlineLinks()
                    ? DocumentTextDecoration.UNDERLINE
                    : DocumentTextDecoration.DEFAULT;
            DocumentTextStyle linkStyle = DocumentTextStyle.builder()
                    .fontName(fontName)
                    .size(size)
                    .color(base.linkColor())
                    .decoration(decoration)
                    .build();
            DocumentLinkOptions options = linkOptionsOrNull(decor.linkUrl());
            if (!code && EmojiShapes.contains(text)) {
                // A geometric emoji inside link text must still become a shape (not a missing
                // glyph); the surrounding words keep the link annotation.
                EmojiShapes.append(rich, text, linkStyle, size, options);
            } else if (options != null) {
                rich.with(text, linkStyle, options);
            } else {
                // Relative / anchor / schemeless href: the engine only annotates absolute-URI
                // links, so render link-styled text without an annotation rather than crash.
                rich.style(text, linkStyle);
            }
            return;
        }

        DocumentColor color = code ? base.codeColor() : base.color();
        DocumentTextDecoration decoration = decor.strike()
                ? DocumentTextDecoration.STRIKETHROUGH
                : DocumentTextDecoration.DEFAULT;
        DocumentTextStyle style = DocumentTextStyle.builder()
                .fontName(fontName)
                .size(size)
                .color(color)
                .decoration(decoration)
                .build();
        // Geometric emoji (🔴 🟩 ⭐ …) have no glyph in PDF fonts and would render as "?";
        // map them to native inline vector shapes. Code stays verbatim (Markdown rule).
        if (!code && EmojiShapes.contains(text)) {
            EmojiShapes.append(rich, text, style, size);
        } else {
            rich.style(text, style);
        }
    }

    /**
     * Link options for an absolute-URI target, or {@code null} for a relative / anchor /
     * schemeless href. {@code DocumentLinkOptions} rejects a schemeless URI (and the engine
     * only annotates absolute links), so a {@code null} here means "render styled text, no
     * annotation" instead of aborting the whole render.
     */
    private static DocumentLinkOptions linkOptionsOrNull(String url) {
        if (url == null || !hasScheme(url)) {
            return null;
        }
        try {
            return new DocumentLinkOptions(url);
        } catch (RuntimeException malformed) {
            return null; // scheme present but the URI is malformed — degrade to styled text
        }
    }

    /** Whether {@code url} begins with a URI scheme ({@code scheme:}), per RFC 3986. */
    private static boolean hasScheme(String url) {
        int colon = url.indexOf(':');
        if (colon <= 0) {
            return false;
        }
        for (int i = 0; i < colon; i++) {
            char c = url.charAt(i);
            boolean ok = i == 0
                    ? Character.isLetter(c)
                    : Character.isLetterOrDigit(c) || c == '+' || c == '.' || c == '-';
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /** Emits a footnote reference as a small accent-coloured {@code [N]} on the baseline. */
    private void emitFootnoteRef(RichText rich, FootnoteRefRun ref, InlineStyle base) {
        DocumentTextStyle style = DocumentTextStyle.builder()
                .fontName(base.family().resolve(false, false))
                .size(base.size() * 0.78)
                .color(base.linkColor())
                .decoration(DocumentTextDecoration.DEFAULT)
                .build();
        rich.style("[" + ref.number() + "]", style);
    }

    /** Accumulated inline decoration state as the tree is walked. */
    private record Decor(boolean bold, boolean italic, boolean strike, String linkUrl) {

        Decor withBold() {
            return new Decor(true, italic, strike, linkUrl);
        }

        Decor withItalic() {
            return new Decor(bold, true, strike, linkUrl);
        }

        Decor withStrike() {
            return new Decor(bold, italic, true, linkUrl);
        }

        Decor withLink(String url) {
            return new Decor(bold, italic, strike, url);
        }
    }
}
