package io.github.demchaav.markdown.composer;

import io.github.demchaav.markdown.model.CodeBlockNode;
import io.github.demchaav.markdown.model.TableNode;
import io.github.demchaav.markdown.render.NodeRenderer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownComposerTest {

    private static final String KITCHEN_SINK = """
            # GraphCompose Markdown

            Intro with **bold**, *italic*, ~~strike~~, `code`, and a [link](https://example.com).

            ## Lists

            - one
            - two
                - nested two-a
            1. first
            2. second

            ## Code

            ```java
            int x = 1;
            System.out.println(x);
            ```

            > A quote with **emphasis** across
            > two lines.

            ---

            :::callout warning
            Be careful with this configuration.
            :::

            ![missing image](does-not-exist.png)
            """;

    private static String header(byte[] pdf) {
        return new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.US_ASCII);
    }

    @Test
    void rendersKitchenSinkToPdfWithLightTheme() throws Exception {
        byte[] pdf = MarkdownComposer.builder()
                .theme(DefaultMarkdownTheme.light())
                .build()
                .render(KITCHEN_SINK)
                .toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
        assertThat(pdf.length).isGreaterThan(1000);
    }

    @Test
    void rendersWithDarkTheme() throws Exception {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.dark())
                .render(KITCHEN_SINK)
                .toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
    }

    @Test
    void rendersEmptyDocumentWithoutError() throws Exception {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("")
                .toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
    }

    @Test
    void rendersTaskListToPdf() throws Exception {
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("- [x] done\n- [ ] todo\n- plain item")
                .toPdfBytes();
        assertThat(header(pdf)).isEqualTo("%PDF-");
    }

    @Test
    void rendersGfmTableToPdf() throws Exception {
        String md = """
                | A | B | C |
                |:--|:-:|--:|
                | 1 | 2 | 3 |
                | 4 | 5 | 6 |
                """;
        MarkdownComposer.Rendered rendered = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(md);

        assertThat(rendered.document().blocks()).anySatisfy(n -> assertThat(n).isInstanceOf(TableNode.class));
        assertThat(header(rendered.toPdfBytes())).isEqualTo("%PDF-");
    }

    @Test
    void composesThemeByOverridingASingleRenderer() throws Exception {
        AtomicBoolean customInvoked = new AtomicBoolean(false);
        NodeRenderer<CodeBlockNode> custom = (node, host, ctx) -> {
            customInvoked.set(true);
            host.addParagraph(p -> p.text("CODE: " + node.code()));
        };

        // Reuse every default renderer from the light theme, override only code blocks.
        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .renderer(CodeBlockNode.class, custom)
                .build();

        byte[] pdf = MarkdownComposer.create(theme)
                .render("Before\n\n```\nhi\n```\n\nAfter")
                .toPdfBytes();

        assertThat(customInvoked).isTrue();
        assertThat(header(pdf)).isEqualTo("%PDF-");
    }
}
