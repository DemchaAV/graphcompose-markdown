package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;

import java.nio.file.Path;

/**
 * GitHub-style alerts ({@code > [!NOTE]} …) plus a navigable PDF outline from headings.
 * Open the viewer's bookmarks/outline pane to see the heading tree.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.AlertsAndOutlineExample
 * </pre>
 */
public final class AlertsAndOutlineExample {

    private AlertsAndOutlineExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown = """
                # Alerts & outline

                Every heading below also becomes a PDF bookmark — open your viewer's
                outline / bookmarks pane to navigate the document.

                ## Note
                > [!NOTE]
                > Useful information that users should know.

                ## Tip
                > [!TIP]
                > Helpful advice for doing things better.

                ## Important
                > [!IMPORTANT]
                > Key information users need to know to succeed.

                ## Warning
                > [!WARNING]
                > Urgent info that needs immediate attention.

                ## Caution
                > [!CAUTION]
                > Advises about risks or negative outcomes.
                """;

        Path out = Path.of("alerts.pdf");
        MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath() + "  (open the bookmarks pane for the outline)");
    }
}
