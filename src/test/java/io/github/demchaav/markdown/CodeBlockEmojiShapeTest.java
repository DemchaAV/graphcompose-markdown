package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Geometric emoji (coloured circles, squares, …) typed inside a fenced code block render as
 * native vector shapes — not the missing-glyph {@code "?"} a PDF mono font would otherwise
 * produce. The surrounding code text stays verbatim.
 */
class CodeBlockEmojiShapeTest {

    @Test
    void geometricEmojiInACodeBlockBecomeShapesNotQuestionMarks() throws Exception {
        String md = """
                ```
                Priority: 🔴 blocker 🟡 important 🟢 nice to have
                ```
                """;

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(md).toPdfBytes();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(doc);
            // The verbatim code text survives intact ...
            assertThat(text)
                    .contains("Priority:")
                    .contains("blocker")
                    .contains("important")
                    .contains("nice to have");
            // ... but each geometric emoji is a vector shape, so neither the codepoint nor a
            // missing-glyph "?" leaks into the text layer.
            assertThat(text)
                    .doesNotContain("🔴")
                    .doesNotContain("🟡")
                    .doesNotContain("🟢")
                    .doesNotContain("?");
        }
    }
}
