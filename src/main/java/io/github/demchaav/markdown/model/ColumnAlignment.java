package io.github.demchaav.markdown.model;

/**
 * Horizontal alignment of a table column, taken from the GFM delimiter row.
 */
public enum ColumnAlignment {
    /** No explicit alignment (renderer default, usually left). */
    NONE,
    /** Left-aligned ({@code :---}). */
    LEFT,
    /** Centre-aligned ({@code :--:}). */
    CENTER,
    /** Right-aligned ({@code ---:}). */
    RIGHT
}
