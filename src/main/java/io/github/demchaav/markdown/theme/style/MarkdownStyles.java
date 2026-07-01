package io.github.demchaav.markdown.theme.style;

import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.extension.CodeTokenType;
import io.github.demchaav.markdown.theme.tokens.FontFamily;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;
import io.github.demchaav.markdown.theme.tokens.SyntaxColors;

import java.util.Objects;

/**
 * Derives per-element component styles from a {@link MarkdownTokens} bundle —
 * layer two of the theme model.
 *
 * <p>Renderers read styles from here instead of touching raw tokens, so the
 * token-to-style mapping lives in one place. The styles are computed on demand;
 * there is no stored state beyond the tokens.</p>
 */
public final class MarkdownStyles {

    private final MarkdownTokens tokens;

    /**
     * Creates a style facade over a token bundle.
     *
     * @param tokens the design tokens; must not be {@code null}
     */
    public MarkdownStyles(MarkdownTokens tokens) {
        this.tokens = Objects.requireNonNull(tokens, "tokens");
    }

    /** @return the underlying design tokens */
    public MarkdownTokens tokens() {
        return tokens;
    }

    /** @return the base inline style for body paragraphs */
    public InlineStyle paragraphInline() {
        return new InlineStyle(
                tokens.typography().bodyFamily(),
                tokens.typography().bodySize(),
                tokens.colors().text(),
                false,
                false,
                tokens.typography().codeFamily(),
                tokens.typography().codeSize(),
                tokens.colors().code(),
                tokens.colors().codeBackground(),
                tokens.colors().link(),
                tokens.shape().underlineLinks());
    }

    /**
     * Returns the base inline style for a heading of the given level.
     *
     * @param level the heading level, 1 through 6
     * @return the inline style (bold, heading font/colour, level-scaled size)
     */
    public InlineStyle headingInline(int level) {
        return new InlineStyle(
                tokens.typography().headingFamily(),
                tokens.typography().headingSize(level),
                tokens.colors().heading(),
                true,
                false,
                tokens.typography().codeFamily(),
                tokens.typography().headingSize(level) * 0.85,
                tokens.colors().code(),
                tokens.colors().codeBackground(),
                tokens.colors().link(),
                tokens.shape().underlineLinks());
    }

    /** @return the base inline style used for plain text inside list items / quotes */
    public InlineStyle bodyInlineFor(DocumentColor textColor) {
        InlineStyle base = paragraphInline();
        return new InlineStyle(base.family(), base.size(), textColor, false, false,
                base.codeFamily(), base.codeSize(), base.codeColor(), base.codeBackground(),
                base.linkColor(), base.underlineLinks());
    }

    /** @return the vertical gap between top-level blocks, in points */
    public double blockSpacing() {
        return tokens.spacing().blockSpacing();
    }

    /**
     * Converts the body line-spacing multiplier into the extra leading (in points) the engine
     * adds between wrapped lines of a paragraph at the given font size. The engine's
     * {@code lineSpacing} is absolute points, so a multiplier of {@code 1.0} yields no extra
     * leading (single spacing) and, e.g., {@code 1.5} adds half the font size.
     *
     * @param fontSize the font size the leading is relative to, in points
     * @return the extra inter-line leading in points (never negative)
     */
    public double lineLeading(double fontSize) {
        double extra = (tokens.typography().lineSpacing() - 1.0) * fontSize;
        return Math.max(0.0, extra);
    }

    /**
     * @param level the heading level
     * @return the space to leave above the heading, in points (0 for the first block, handled by the renderer)
     */
    public double headingSpaceAbove(int level) {
        return tokens.spacing().headingSpaceAbove();
    }

    /**
     * @param level the heading level
     * @return the space to leave below the heading, in points
     */
    public double headingSpaceBelow(int level) {
        return tokens.spacing().headingSpaceBelow();
    }

    /**
     * Returns the colour for a code syntax token kind; plain code uses the code text colour.
     *
     * @param type the token kind
     * @return the colour to paint the token in
     */
    public DocumentColor syntaxColor(CodeTokenType type) {
        SyntaxColors colors = tokens.syntax();
        switch (type) {
            case KEYWORD:
                return colors.keyword();
            case STRING:
                return colors.string();
            case COMMENT:
                return colors.comment();
            case NUMBER:
                return colors.number();
            case ANNOTATION:
                return colors.annotation();
            case FUNCTION:
                return colors.function();
            default:
                return tokens.colors().code();
        }
    }

    /** @return the code-block component style */
    public CodeBlockStyle codeBlock() {
        return new CodeBlockStyle(
                tokens.colors().codeBackground(),
                tokens.typography().codeFamily(),
                tokens.typography().codeSize(),
                tokens.colors().code(),
                tokens.spacing().codePadding(),
                tokens.shape().panelCornerRadius(),
                tokens.typography().lineSpacing());
    }

    /** @return the blockquote component style */
    public QuoteStyle quote() {
        return new QuoteStyle(
                tokens.colors().quoteBar(),
                tokens.spacing().quoteAccentWidth(),
                tokens.spacing().quotePadding(),
                tokens.colors().quoteText(),
                tokens.colors().codeBackground(),
                tokens.shape().panelCornerRadius());
    }

    /** @return the list component style */
    public ListStyle list() {
        return new ListStyle(
                tokens.spacing().listIndent(),
                tokens.spacing().listItemSpacing(),
                tokens.colors().muted());
    }

    /** @return the horizontal-rule component style */
    public RuleStyle rule() {
        return new RuleStyle(
                tokens.colors().rule(),
                tokens.shape().ruleThickness(),
                tokens.page().contentWidth());
    }

    /** @return the base callout component style (variant tinting is the renderer's job) */
    public CalloutStyle callout() {
        return new CalloutStyle(
                tokens.colors().accent(),
                tokens.spacing().calloutAccentWidth(),
                tokens.spacing().calloutPadding(),
                tokens.shape().panelCornerRadius());
    }

    /** @return the table component style */
    public TableStyle table() {
        return new TableStyle(
                tokens.colors().codeBackground(),
                tokens.colors().tableRowBackground(),
                tokens.colors().heading(),
                tokens.colors().text(),
                tokens.colors().rule(),
                Math.max(0.5, tokens.shape().ruleThickness()),
                tokens.spacing().tableCellPadding(),
                tokens.typography().bodyFamily(),
                tokens.typography().bodySize());
    }

    /**
     * Code-block panel style.
     *
     * @param background   panel fill colour
     * @param family       code font family
     * @param size         code font size in points
     * @param textColor    code text colour
     * @param padding      inner padding in points
     * @param cornerRadius panel corner radius in points
     * @param lineSpacing  line-spacing multiplier
     */
    public record CodeBlockStyle(
            DocumentColor background,
            FontFamily family,
            double size,
            DocumentColor textColor,
            double padding,
            double cornerRadius,
            double lineSpacing) {
    }

    /**
     * Blockquote style.
     *
     * @param barColor     left accent bar colour
     * @param barWidth     left accent bar width in points
     * @param padding      inner padding in points
     * @param textColor    quoted text colour
     * @param background   plate background fill
     * @param cornerRadius plate corner radius in points
     */
    public record QuoteStyle(DocumentColor barColor, double barWidth, double padding,
                             DocumentColor textColor, DocumentColor background, double cornerRadius) {
    }

    /**
     * List style.
     *
     * @param indent       indent per nesting level in points
     * @param itemSpacing  gap between items in points
     * @param markerColor  colour of the list marker (bullet / number / checkbox)
     */
    public record ListStyle(double indent, double itemSpacing, DocumentColor markerColor) {
    }

    /**
     * Horizontal-rule style.
     *
     * @param color     line colour
     * @param thickness line thickness in points
     * @param width     line width in points
     */
    public record RuleStyle(DocumentColor color, double thickness, double width) {
    }

    /**
     * Callout (custom block) style.
     *
     * @param accent       default accent colour
     * @param accentWidth  left accent bar width in points
     * @param padding      inner padding in points
     * @param cornerRadius panel corner radius in points
     */
    public record CalloutStyle(DocumentColor accent, double accentWidth, double padding, double cornerRadius) {
    }

    /**
     * Table style.
     *
     * @param headerFill  header row background fill
     * @param rowFill     body row background fill
     * @param headerText  header text colour (rendered bold)
     * @param bodyText    body cell text colour
     * @param border      cell border colour
     * @param borderWidth cell border width in points
     * @param cellPadding inner padding of each cell in points
     * @param family      cell font family
     * @param fontSize    cell font size in points
     */
    public record TableStyle(
            DocumentColor headerFill,
            DocumentColor rowFill,
            DocumentColor headerText,
            DocumentColor bodyText,
            DocumentColor border,
            double borderWidth,
            double cellPadding,
            FontFamily family,
            double fontSize) {
    }
}
