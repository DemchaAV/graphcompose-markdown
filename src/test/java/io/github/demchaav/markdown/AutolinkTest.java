package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bare URLs and emails in plain text are turned into links (GFM autolinking).
 */
class AutolinkTest {

    private static MarkdownDocument model(String markdown) {
        return MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).document();
    }

    private static ParagraphNode firstParagraph(MarkdownDocument doc) {
        return (ParagraphNode) doc.blocks().get(0);
    }

    @Test
    void aBareHttpUrlBecomesALink() {
        ParagraphNode paragraph = firstParagraph(model("Visit https://example.com today"));

        assertThat(paragraph.content())
                .anyMatch(n -> n instanceof LinkRun link && link.url().equals("https://example.com"));
    }

    @Test
    void aBareEmailBecomesALink() {
        ParagraphNode paragraph = firstParagraph(model("Contact me@example.com please"));

        LinkRun link = (LinkRun) paragraph.content().stream()
                .filter(LinkRun.class::isInstance).findFirst().orElseThrow();
        assertThat(link.url()).contains("me@example.com");
    }

    @Test
    void plainTextWithoutUrlsIsUnaffected() {
        ParagraphNode paragraph = firstParagraph(model("just some plain prose, no links here"));

        assertThat(paragraph.content()).noneMatch(LinkRun.class::isInstance);
    }

    @Test
    void anAutolinkedUrlRendersToPdf() throws Exception {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("See https://example.com for details.").toPdfBytes();

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
