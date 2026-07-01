package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.inline.StrongRun;
import io.github.demchaav.markdown.model.inline.TextRun;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Opt-in typographic smart punctuation ({@code builder().smartPunctuation(true)}): dashes,
 * ellipsis and curly quotes. Off by default (matching GitHub), and code stays verbatim.
 */
class SmartPunctuationTest {

    private static String text(MarkdownComposer composer, String md) throws Exception {
        try (PDDocument doc = Loader.loadPDF(composer.render(md).toPdfBytes())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    @Test
    void smartPunctuationReplacesDashesEllipsisAndQuotes() throws Exception {
        MarkdownComposer composer = MarkdownComposer.builder().smartPunctuation(true).build();

        String out = text(composer, "Pages 3--7 --- \"quoted\" and 'single', wait...");

        assertThat(out).contains("–");  // en-dash from --
        assertThat(out).contains("—");  // em-dash from ---
        assertThat(out).contains("…");  // ellipsis from ...
        assertThat(out).contains("“").contains("”"); // curly double quotes
        assertThat(out).contains("‘").contains("’"); // curly single quotes
        assertThat(out).doesNotContain("--").doesNotContain("...");
    }

    @Test
    void offByDefaultKeepsLiteralPunctuation() throws Exception {
        MarkdownComposer composer = MarkdownComposer.create(DefaultMarkdownTheme.light());

        String out = text(composer, "Pages 3--7 \"quoted\" wait...");

        assertThat(out).contains("3--7").contains("\"quoted\"").contains("wait...");
        assertThat(out).doesNotContain("–").doesNotContain("…");
    }

    @Test
    void codeStaysVerbatimEvenWithSmartPunctuationOn() throws Exception {
        MarkdownComposer composer = MarkdownComposer.builder().smartPunctuation(true).build();

        String out = text(composer, "Run `a --flag \"x\"` and:\n\n```\nb --opt 'y'...\n```\n");

        assertThat(out).contains("--flag").contains("--opt");
        assertThat(out).contains("'y'...");
        assertThat(out).doesNotContain("–").doesNotContain("—");
    }

    @Test
    void formattingInsideSmartQuotesSurvives() throws Exception {
        MarkdownComposer composer = MarkdownComposer.builder().smartPunctuation(true).build();

        String out = text(composer, "\"a **bold** word\"");

        // The quotes wrap mapped children (bold still renders); marks are curly.
        assertThat(out).contains("“").contains("bold").contains("”");
    }

    @Test
    void apostrophesCurlAndGuillemetsConvert() throws Exception {
        // The unmatched-single-quote (apostrophe) path is distinct from paired quotes,
        // and <<text>> converts to guillemets.
        MarkdownComposer composer = MarkdownComposer.builder().smartPunctuation(true).build();

        String out = text(composer, "It don't matter <<much>> here");

        assertThat(out).contains("don’t").contains("«much»");
        assertThat(out).doesNotContain("&"); // no entity text ever leaks into the PDF
    }

    @Test
    void replacementsApplyInsideCustomBlocksToo() throws Exception {
        // The ::: segmented parse must use the same flag-configured parser instance.
        MarkdownComposer composer = MarkdownComposer.builder().smartPunctuation(true).build();

        String out = text(composer, """
                :::note
                range 3--7 wait...
                :::

                outside 1--2
                """);

        assertThat(out).contains("3–7").contains("…").contains("1–2");
        assertThat(out).doesNotContain("--");
    }

    @Test
    void boldInsideSmartQuotesSurvivesAtTheModelLevel() {
        // Model-level proof (extraction can't distinguish bold): TextRun(curly-open) +
        // StrongRun + ... + TextRun(curly-close).
        MarkdownComposer composer = MarkdownComposer.builder().smartPunctuation(true).build();

        var blocks = composer.render("\"a **bold** word\"").document().blocks();
        var paragraph = (ParagraphNode) blocks.get(0);
        var runs = paragraph.content();

        assertThat(runs.get(0)).isInstanceOfSatisfying(TextRun.class,
                r -> assertThat(r.text()).isEqualTo("“"));
        assertThat(runs).anyMatch(r -> r instanceof StrongRun);
        assertThat(runs.get(runs.size() - 1)).isInstanceOfSatisfying(TextRun.class,
                r -> assertThat(r.text()).isEqualTo("”"));
    }
}
