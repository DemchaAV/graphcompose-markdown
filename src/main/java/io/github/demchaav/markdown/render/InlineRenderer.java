package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.node.DocumentLinkOptions;
import com.demcha.compose.font.FontName;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentTextDecoration;
import com.demcha.compose.document.style.DocumentTextStyle;
import io.github.demchaav.markdown.model.inline.CodeRun;
import io.github.demchaav.markdown.model.inline.EmphasisRun;
import io.github.demchaav.markdown.model.inline.ImageRun;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.model.inline.LineBreakRun;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.model.inline.StrikethroughRun;
import io.github.demchaav.markdown.model.inline.StrongRun;
import io.github.demchaav.markdown.model.inline.TextRun;
import io.github.demchaav.markdown.theme.style.InlineStyle;
import io.github.demchaav.markdown.theme.tokens.FontFamily;

import java.util.List;

/**
 * Flattens a tree of {@link InlineNode}s into a GraphCompose {@link RichText}.
 *
 * <p>Bold and italic are accumulated down the tree and applied via the font
 * variant; strikethrough uses the decoration enum, so the two compose freely.
 * Inline code switches to the code font and colour. Links are emitted via
 * {@link RichText#link(String, String)}; formatting inside a link degrades to
 * the link style.</p>
 */
public final class InlineRenderer {

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
        } else if (node instanceof LineBreakRun lineBreak) {
            if (lineBreak.hard()) {
                rich.plain("\n");
            } else {
                rich.space();
            }
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
            rich.with(text, linkStyle, new DocumentLinkOptions(decor.linkUrl()));
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
        rich.style(text, style);
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
