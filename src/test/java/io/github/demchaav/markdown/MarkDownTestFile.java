package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Ad-hoc harness for eyeballing how arbitrary Markdown files render: it drops every
 * {@code *.md} in the repo-root {@code resource/} folder through {@link MarkdownComposer}
 * and writes a {@code <name>.pdf} next to it (PDFs are git-ignored), asserting each one is
 * a valid, non-empty PDF.
 *
 * <p>Workflow: put one or more {@code .md} files in {@code resource/} and run this test —
 * each becomes its own dynamic test named after the file. With the folder absent or empty
 * the test is <em>skipped</em>, so it never fails the suite on a clean checkout. Relative
 * images in a file resolve against {@code resource/}; switch {@link #THEME} to render in a
 * different look (e.g. {@code DefaultMarkdownTheme.dark()} or a theme pack) and re-run.</p>
 *
 * <p><b>Run it explicitly</b> — the class name does not match Surefire's default
 * {@code *Test} / {@code Test*} pattern, so a plain {@code mvn test} (and CI) skips it. That
 * is intentional: it is a manual eyeballing harness, not a CI gate, and should not write PDFs
 * into {@code resource/} on every build. Invoke it on demand:
 * <pre>./mvnw test -Dtest=MarkDownTestFile</pre>
 * (rename it to {@code MarkDownFileTest} if you ever want it in the normal suite.)</p>
 */
class MarkDownTestFile {

    /** Repo-root folder you drop Markdown files into; each {@code *.md} is rendered to a sibling PDF. */
    private static final Path RESOURCE_DIR = Path.of("resource");

    /** Theme used to render — swap to {@code DefaultMarkdownTheme.dark()} or a pack to compare looks. */
    private static final MarkdownTheme THEME = DefaultMarkdownTheme.light();

    @TestFactory
    Stream<DynamicTest> rendersEveryMarkdownFileInResourceDir() throws IOException {
        assumeTrue(Files.isDirectory(RESOURCE_DIR),
                "Put Markdown files in '" + RESOURCE_DIR.toAbsolutePath() + "' to render them.");

        List<Path> markdownFiles;
        try (Stream<Path> entries = Files.list(RESOURCE_DIR)) {
            markdownFiles = entries
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".md"))
                    .sorted()
                    .toList();
        }
        assumeTrue(!markdownFiles.isEmpty(),
                "No .md files in '" + RESOURCE_DIR.toAbsolutePath() + "' — drop some in and re-run.");

        MarkdownComposer composer = MarkdownComposer.create(THEME);

        return markdownFiles.stream().map(md -> DynamicTest.dynamicTest(md.getFileName().toString(), () -> {
            // renderFile reads the file (UTF-8) and resolves relative images against its own directory.
            Path out = md.resolveSibling(stem(md) + ".pdf");
            composer.renderFile(md).writePdf(out);

            assertThat(out).as("a PDF was written for %s", md.getFileName()).exists();
            assertThat(Files.size(out))
                    .as("%s produced a non-trivial PDF", out.getFileName())
                    .isGreaterThan(500L);
            try (PDDocument rendered = Loader.loadPDF(out.toFile())) {
                assertThat(rendered.getNumberOfPages())
                        .as("%s has at least one page", out.getFileName())
                        .isGreaterThanOrEqualTo(1);
            }
            System.out.println("Rendered " + md.getFileName() + " -> " + out.toAbsolutePath()
                    + " (" + Files.size(out) + " bytes)");
        }));
    }

    /** The file name without its extension, e.g. {@code notes.md -> notes}. */
    private static String stem(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}
