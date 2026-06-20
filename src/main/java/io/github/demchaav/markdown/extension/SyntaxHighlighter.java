package io.github.demchaav.markdown.extension;

import java.util.List;

/**
 * Tokenises fenced code-block text for syntax highlighting.
 *
 * <p>This is the extension seam for highlighting. The built-in
 * {@link RegexSyntaxHighlighter} covers common languages with no extra
 * dependency; supply a custom highlighter (e.g. backed by a TextMate / Pygments
 * engine) via the theme for full fidelity. The returned tokens, concatenated,
 * must equal {@code code} verbatim.</p>
 */
@FunctionalInterface
public interface SyntaxHighlighter {

    /**
     * Tokenises code into typed runs.
     *
     * @param code     the literal code text
     * @param language the language hint from the fence info string (may be empty)
     * @return the tokens, in order, whose concatenation equals {@code code}
     */
    List<CodeToken> highlight(String code, String language);
}
