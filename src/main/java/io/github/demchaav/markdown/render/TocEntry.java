package io.github.demchaav.markdown.render;

/**
 * One heading's entry in the table of contents: its level (1–6), its plain text, and the
 * GitHub-style slug anchor it links to (the same slug the heading declares, so the link resolves).
 *
 * @param level the heading level, 1 through 6
 * @param text  the heading's plain text (inline markup stripped)
 * @param slug  the heading's anchor slug
 */
record TocEntry(int level, String text, String slug) {
}
