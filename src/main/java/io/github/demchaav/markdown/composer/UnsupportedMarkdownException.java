package io.github.demchaav.markdown.composer;

/**
 * Thrown by a composer in {@linkplain MarkdownComposer.Builder#strictMode(boolean)
 * strict mode} when a document contains content the library cannot render faithfully
 * (an unsupported block or inline element). In the default lenient mode the content
 * is surfaced as raw text instead, and this is never thrown.
 */
public class UnsupportedMarkdownException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message describes the unsupported content
     */
    public UnsupportedMarkdownException(String message) {
        super(message);
    }
}
