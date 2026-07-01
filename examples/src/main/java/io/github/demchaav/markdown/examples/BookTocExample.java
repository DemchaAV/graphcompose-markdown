package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.TocNode;
import io.github.demchaav.markdown.render.BookTocRenderer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.FooterTokens;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A book-style, page-numbered table of contents: swap the {@code TocNode} renderer for
 * {@link BookTocRenderer} and a {@code [TOC]} marker renders as dot-leader rows —
 * "Introduction ..... 3" — with the page numbers resolved automatically from the laid-out
 * document and every label a clickable jump. Paired here with the page-number footer, the
 * classic report look. The default renderer (a plain clickable link list) stays as-is for
 * screen-oriented documents.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.BookTocExample
 * </pre>
 */
public final class BookTocExample {

    private BookTocExample() {
    }

    public static void main(String[] args) throws Exception {
        String filler = IntStream.range(0, 45)
                .mapToObj(i -> "Filler prose paragraph so the chapters land on later pages.")
                .collect(Collectors.joining("\n\n"));
        String markdown = "[TOC]\n\n# Introduction\n\nWelcome.\n\n" + filler
                + "\n\n# Getting started\n\nSetup steps.\n\n## Configuration\n\nDetails.\n\n" + filler
                + "\n\n# Appendix\n\nReference material.\n";

        MarkdownTheme base = DefaultMarkdownTheme.light();
        MarkdownTheme bookTheme = MarkdownTheme.builder(base)
                .renderer(TocNode.class, new BookTocRenderer("Contents")) // page-numbered TOC
                .tokens(base.tokens().withFooter(FooterTokens.pageNumbers())) // matching footer
                .build();

        Path out = Path.of("book-toc.pdf");
        MarkdownComposer.create(bookTheme).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath() + "  (dot-leader contents with live page numbers)");
    }
}
