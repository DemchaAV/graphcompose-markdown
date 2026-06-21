package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.AlertNode;
import io.github.demchaav.markdown.model.AlertType;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.QuoteNode;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GitHub-style alerts: a blockquote whose first line is {@code [!TYPE]} renders as a
 * titled, colour-coded callout; everything else stays a plain blockquote.
 */
class GithubAlertsTest {

    private static MarkdownDocument model(String markdown) {
        return MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).document();
    }

    @Test
    void blockquoteWithAnAlertMarkerBecomesAnAlert() {
        MarkdownDocument doc = model("> [!WARNING]\n> Be careful here.");

        AlertNode alert = (AlertNode) doc.blocks().stream()
                .filter(AlertNode.class::isInstance).findFirst().orElseThrow();
        assertThat(alert.type()).isEqualTo(AlertType.WARNING);
        assertThat(doc.blocks()).noneMatch(QuoteNode.class::isInstance);
    }

    @Test
    void allFiveAlertTypesParse() {
        for (AlertType type : AlertType.values()) {
            MarkdownDocument doc = model("> [!" + type.name() + "]\n> body text");
            assertThat(doc.blocks()).as("%s", type)
                    .anyMatch(b -> b instanceof AlertNode alert && alert.type() == type);
        }
    }

    @Test
    void aPlainBlockquoteStaysAQuote() {
        MarkdownDocument doc = model("> just an ordinary quote");

        assertThat(doc.blocks()).anyMatch(QuoteNode.class::isInstance);
        assertThat(doc.blocks()).noneMatch(AlertNode.class::isInstance);
    }

    @Test
    void theMarkerMustBeAloneOnTheFirstLine() {
        // GitHub requires the marker on its own line; "[!NOTE] text" is a normal quote.
        MarkdownDocument doc = model("> [!NOTE] this is not an alert");

        assertThat(doc.blocks()).anyMatch(QuoteNode.class::isInstance);
        assertThat(doc.blocks()).noneMatch(AlertNode.class::isInstance);
    }

    @Test
    void alertRendersWithTitleAndBodyButNotTheRawMarker() throws Exception {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("> [!TIP]\n> Derive a theme and override only what differs.")
                .toPdfBytes();

        String text = extractText(pdf);
        assertThat(text).contains("Tip");
        assertThat(text).contains("Derive a theme");
        assertThat(text).doesNotContain("[!TIP]");
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }
}
