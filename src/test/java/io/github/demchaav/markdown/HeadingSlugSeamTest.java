package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.render.TocEntry;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The custom-renderer seam documented in {@code docs/custom-renderers.md} must be reachable for
 * headings and the TOC from outside the render package. A third-party {@code NodeRenderer<HeadingNode>}
 * needs the <em>planned</em> slug (so its anchor matches what {@code [TOC]} / {@code [text](#slug)}
 * jump to), and a {@code NodeRenderer<TocNode>} needs the planned entries — hence
 * {@link io.github.demchaav.markdown.render.RenderContext#headingSlug} /
 * {@link io.github.demchaav.markdown.render.RenderContext#tocEntries} and {@link TocEntry} are public.
 * Living outside {@code io.github.demchaav.markdown.render}, this test would not compile if the seam
 * were still package-private.
 */
class HeadingSlugSeamTest {

    @Test
    void aCustomHeadingRendererSeesThePlannedSlugAndTocEntries() {
        List<String> slugs = new ArrayList<>();
        List<TocEntry> toc = new ArrayList<>();

        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .renderer(HeadingNode.class, (node, host, ctx) -> {
                    slugs.add(ctx.headingSlug(node));
                    if (toc.isEmpty()) {
                        toc.addAll(ctx.tocEntries());
                    }
                    host.addParagraph(p -> p
                            .rich(ctx.toRich(node.content(), ctx.headingInline(node.level())))
                            .anchor(ctx.headingSlug(node)));
                })
                .build();

        MarkdownComposer.builder().theme(theme).build()
                .render("# Hello World\n\n## Hello World\n").toPdfBytes();

        // The planned, document-unique GitHub slugs — the same ones [TOC] links to.
        assertThat(slugs).containsExactly("hello-world", "hello-world-1");
        assertThat(toc).extracting(TocEntry::slug).containsExactly("hello-world", "hello-world-1");
        assertThat(toc).extracting(TocEntry::text).containsExactly("Hello World", "Hello World");
    }

    @Test
    void generatedDuplicateSlugsNeverCollide() {
        List<String> slugs = new ArrayList<>();

        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .renderer(HeadingNode.class, (node, host, ctx) -> {
                    slugs.add(ctx.headingSlug(node));
                    host.addParagraph(p -> p.rich(ctx.toRich(node.content(), ctx.headingInline(node.level()))));
                })
                .build();

        // "Foo", "Foo", "Foo 1" all want the slug "foo-1"; every heading must still get a unique one.
        MarkdownComposer.builder().theme(theme).build()
                .render("# Foo\n\n# Foo\n\n# Foo 1\n").toPdfBytes();

        assertThat(slugs).doesNotContainNull();
        assertThat(slugs).as("every heading gets a document-unique anchor").doesNotHaveDuplicates();
    }
}
