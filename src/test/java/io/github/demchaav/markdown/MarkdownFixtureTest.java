package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.packs.AcademicTheme;
import io.github.demchaav.markdown.theme.packs.BusinessReportTheme;
import io.github.demchaav.markdown.theme.packs.GitHubTheme;
import io.github.demchaav.markdown.theme.packs.MinimalTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fixture-driven smoke tests over every Markdown file in
 * {@code src/test/resources/markdown/}. Each fixture must:
 *
 * <ol>
 *   <li>parse without error (into a non-empty semantic tree),</li>
 *   <li>render to a PDF that begins with {@code %PDF-},</li>
 *   <li>be non-empty and have at least one page,</li>
 *   <li>carry its expected text into the rendered output.</li>
 * </ol>
 *
 * <p>{@code master.md} is the canonical document that exercises the whole parser;
 * it is additionally rendered through every theme pack. Visual / snapshot tests can
 * layer on top of these same fixtures later.</p>
 */
class MarkdownFixtureTest {

    /** A fixture file plus words that must survive into the rendered PDF text. */
    private record Fixture(String file, List<String> expectedText) {
    }

    private static final List<Fixture> FIXTURES = List.of(
            new Fixture("basic.md", List.of("Basic document", "paragraph", "Second heading")),
            new Fixture("lists.md", List.of("First bullet", "Nested bullet", "Second step",
                    "Completed task", "Pending task")),
            new Fixture("table.md", List.of("Feature", "Status", "Headings", "stable", "Tables")),
            new Fixture("code.md", List.of("snippet", "public class Demo", "println", "plain text line")),
            new Fixture("quote.md", List.of("quoted wisdom", "Spanning two lines",
                    "Careful with this configuration", "override only what differs")),
            new Fixture("mixed-inline.md", List.of("bold text", "italic text", "struck text",
                    "inline code", "labelled link")),
            new Fixture("master.md", List.of("GraphCompose Markdown", "Heading level six",
                    "Ship GFM tables", "writePdf", "Feature", "Themes decide")));

    @TestFactory
    Stream<DynamicTest> everyFixtureParsesRendersAndCarriesItsText() {
        MarkdownComposer composer = MarkdownComposer.create(DefaultMarkdownTheme.light());
        return FIXTURES.stream().map(fx -> DynamicTest.dynamicTest(fx.file(), () -> {
            String markdown = load(fx.file());

            // 1. parses without error -> a non-empty semantic tree
            MarkdownComposer.Rendered rendered = composer.render(markdown);
            assertThat(rendered.document().blocks()).as("%s parsed to blocks", fx.file()).isNotEmpty();

            // 2-3. renders a non-empty PDF with at least one page
            byte[] pdf = rendered.toPdfBytes();
            assertThat(header(pdf)).as("%s is a PDF", fx.file()).isEqualTo("%PDF-");
            assertThat(pdf.length).as("%s PDF non-empty", fx.file()).isGreaterThan(1000);
            assertThat(pageCount(pdf)).as("%s has pages", fx.file()).isGreaterThanOrEqualTo(1);

            // 4. the text is approximately there
            String text = normalize(extractText(pdf));
            for (String marker : fx.expectedText()) {
                assertThat(text).as("%s renders text '%s'", fx.file(), marker).contains(marker);
            }
        }));
    }

    @Test
    void masterPageRendersThroughEveryThemePack() throws IOException {
        String markdown = load("master.md");
        List<MarkdownTheme> themes = List.of(
                DefaultMarkdownTheme.light(),
                DefaultMarkdownTheme.dark(),
                GitHubTheme.light(),
                GitHubTheme.dark(),
                AcademicTheme.light(),
                MinimalTheme.light(),
                BusinessReportTheme.light());

        for (MarkdownTheme theme : themes) {
            byte[] pdf = MarkdownComposer.create(theme).render(markdown).toPdfBytes();

            assertThat(header(pdf)).isEqualTo("%PDF-");
            assertThat(pageCount(pdf)).isGreaterThanOrEqualTo(1);
            assertThat(normalize(extractText(pdf))).contains("GraphCompose");
        }
    }

    // --- helpers ---

    private static String load(String fixture) throws IOException {
        try (InputStream in = MarkdownFixtureTest.class.getResourceAsStream("/markdown/" + fixture)) {
            assertThat(in).as("fixture %s on the classpath", fixture).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String header(byte[] pdf) {
        return new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.US_ASCII);
    }

    private static int pageCount(byte[] pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return doc.getNumberOfPages();
        }
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    /** Collapse whitespace and turn the code panel's non-breaking spaces back into spaces. */
    private static String normalize(String text) {
        return text.replace(' ', ' ').replaceAll("\\s+", " ").trim();
    }
}
