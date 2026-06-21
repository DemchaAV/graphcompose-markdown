package io.github.demchaav.markdown.model;

/**
 * The five GitHub-style alert kinds (`> [!NOTE]`, `> [!TIP]`, …). Each carries the
 * title shown at the top of the rendered alert.
 */
public enum AlertType {

    /** Useful information the reader should notice. */
    NOTE("Note"),
    /** Helpful advice. */
    TIP("Tip"),
    /** Key information the reader needs. */
    IMPORTANT("Important"),
    /** Something needing immediate attention. */
    WARNING("Warning"),
    /** A risk of a negative outcome. */
    CAUTION("Caution");

    private final String title;

    AlertType(String title) {
        this.title = title;
    }

    /** @return the human title rendered as the alert's heading (e.g. {@code "Note"}) */
    public String title() {
        return title;
    }
}
