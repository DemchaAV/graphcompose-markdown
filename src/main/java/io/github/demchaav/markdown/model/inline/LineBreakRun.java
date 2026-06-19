package io.github.demchaav.markdown.model.inline;

/**
 * A line break inside a block.
 *
 * <p>A soft break (a plain newline in the source) is normally rendered as a
 * space; a hard break (two trailing spaces or a backslash) forces a new line.</p>
 *
 * @param hard {@code true} for a hard break, {@code false} for a soft break
 */
public record LineBreakRun(boolean hard) implements InlineNode {
}
