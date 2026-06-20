package io.github.demchaav.markdown.examples;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.render.NodeRenderer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;

import java.nio.file.Path;

/**
 * Registers a renderer for a custom {@code :::note} block — the extension point for
 * project-specific block types. Everything else reuses the default theme.
 *
 * <pre>
 *   ../mvnw -f pom.xml exec:java \
 *     -Dexec.mainClass=io.github.demchaav.markdown.examples.CustomBlockExample
 * </pre>
 */
public final class CustomBlockExample {

    private CustomBlockExample() {
    }

    public static void main(String[] args) throws Exception {
        // A renderer for ":::note ... :::": a label line, then the block's own children
        // (rendered back through the registry, so they pick up the theme's styling).
        NodeRenderer<CustomBlockNode> note = (node, host, ctx) -> {
            host.addParagraph(p -> p.text("NOTE"));
            ctx.renderBlocks(node.content(), host);
        };

        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .customBlock("note", note)
                .build();

        String markdown = """
                # Custom blocks

                Register a renderer for your own `:::` type and reuse everything else.

                :::note
                This block is rendered by a **custom** NodeRenderer bound to the
                `note` type — its content still flows through the default renderers.
                :::
                """;

        Path out = Path.of("custom-block.pdf");
        MarkdownComposer.create(theme).render(markdown).writePdf(out);
        System.out.println("Wrote " + out.toAbsolutePath());
    }
}
