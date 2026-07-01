package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;

import java.nio.file.Path;

/**
 * A standalone {@code [TOC]} (or {@code [[_TOC_]]}) line auto-generates a clickable table of
 * contents — one link per heading, nested by level, each a native in-document jump. Drop it
 * anywhere, including above the headings it lists (slugs are planned up front). Open the PDF and
 * click a contents entry to jump to its section.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.TocExample
 * </pre>
 */
public final class TocExample {

    private TocExample() {
    }

    public static void main(String[] args) throws Exception {
        String markdown = """
                # Deployment Guide

                [TOC]

                ## Prerequisites

                You need Java 17 and a database.

                ## Configuration

                ### Environment variables

                Set `DB_URL` and `DB_PASSWORD`.

                ### Secrets

                Store secrets in the vault, never in the repo.

                ## Deployment

                Run the deploy script and watch the health check.

                ## Rollback

                Roll back to the previous image tag if the health check fails.
                """;

        Path out = Path.of("toc.pdf");
        MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath() + "  (click the [TOC] entries to jump)");
    }
}
