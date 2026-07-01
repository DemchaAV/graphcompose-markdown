package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;

import java.nio.file.Path;

/**
 * In-PDF navigation (GraphCompose 1.9): a {@code [text](#heading)} link becomes a native
 * PDF go-to action, footnotes are clickable in both directions, and inline {@code code}
 * renders on a rounded chip. Open the PDF and click the Contents entries and the footnote
 * markers — they jump within the document.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.InPdfNavigationExample
 * </pre>
 */
public final class InPdfNavigationExample {

    private InPdfNavigationExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown = """
                # Contents

                - [Overview](#overview)
                - [Usage](#usage)
                - [Notes](#notes)

                Every link above is a native in-document jump — not plain text.

                # Overview

                GraphCompose **1.9** adds in-PDF navigation. Each heading declares a
                GitHub-style anchor, so `[Overview](#overview)` lands right here.[^nav]

                Jump back up to the [Contents](#contents).

                # Usage

                Render as usual — `MarkdownComposer.create(theme)` then `render(md)`. Inline
                code such as `composer.render(md).writePdf(path)` now sits on a rounded chip.[^api]

                # Notes

                Footnotes are bidirectional: click a `[1]` marker to jump to the note, then
                click the marker in the note to jump back to where it was cited.

                [^nav]: The anchor / internal-link API is new in GraphCompose 1.9.
                [^api]: Rendered with graph-compose-markdown on the 1.9 engine.
                """;

        Path out = Path.of("in-pdf-navigation.pdf");
        MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath() + "  (click the Contents links and footnote markers)");
    }
}
