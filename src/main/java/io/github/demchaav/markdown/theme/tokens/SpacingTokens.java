package io.github.demchaav.markdown.theme.tokens;

/**
 * Spacing tokens — gaps and paddings, all in points.
 *
 * @param blockSpacing        vertical gap between top-level blocks
 * @param headingSpaceAbove   extra space above a heading
 * @param headingSpaceBelow   space below a heading
 * @param listItemSpacing     gap between list items
 * @param listIndent          indent applied per list nesting level
 * @param codePadding         inner padding of a code block panel
 * @param quotePadding        inner padding of a blockquote
 * @param quoteAccentWidth    width of the blockquote left accent bar
 * @param calloutPadding      inner padding of a custom-block callout
 * @param calloutAccentWidth  width of the callout left accent bar
 * @param tableCellPadding    inner padding of a table cell
 */
public record SpacingTokens(
        double blockSpacing,
        double headingSpaceAbove,
        double headingSpaceBelow,
        double listItemSpacing,
        double listIndent,
        double codePadding,
        double quotePadding,
        double quoteAccentWidth,
        double calloutPadding,
        double calloutAccentWidth,
        double tableCellPadding) {
}
