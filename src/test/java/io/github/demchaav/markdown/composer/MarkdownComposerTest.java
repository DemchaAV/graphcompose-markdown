package io.github.demchaav.markdown.composer;

import com.vladsch.flexmark.parser.Parser;
import io.github.demchaav.markdown.extension.BundledFonts;
import io.github.demchaav.markdown.model.CodeBlockNode;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.FootnotesNode;
import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.TableNode;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.model.inline.TextRun;
import io.github.demchaav.markdown.render.NodeRenderer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void rendersAPreParsedFlexmarkDocumentDirectly() throws Exception {
        // A caller who already holds a Flexmark tree (their own parser) renders it without
        // a string round-trip.
        Parser flexmark = Parser.builder().build();
        com.vladsch.flexmark.util.ast.Document tree = flexmark.parse("# Title\n\nA **paragraph**.");

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(tree).toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
    }

    @Test
    void rendersAPreBuiltSemanticDocument() throws Exception {
        // No parser at all — build the model by hand and render it.
        MarkdownDocument document = new MarkdownDocument(List.of(
                new HeadingNode(1, List.of(new TextRun("Built by hand"))),
                new ParagraphNode(List.of(new TextRun("No parser involved.")))));

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light()).render(document).toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
    }

    @Test
    void rendersAHighlightedCodeBlockToPdf() throws Exception {
        String md = "```java\n// a note\nint x = 0xFF;\nString s = \"hi\";\n@Override void f() {}\n```";

        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.dark()).render(md).toPdfBytes();

        assertThat(header(pdf)).isEqualTo("%PDF-");
    }

    @Test
    void normalisesCrlfLineEndingsInCodeBlocks() throws Exception {
        // Windows-authored Markdown keeps interior CRLF in the fenced content; it must not
        // survive into the code model, or each rendered line keeps a stray carriage return.
        MarkdownComposer.Rendered rendered = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("```\r\nline one\r\nline two\r\n```");

        CodeBlockNode code = (CodeBlockNode) rendered.document().blocks().stream()
                .filter(CodeBlockNode.class::isInstance).findFirst().orElseThrow();
        assertThat(code.code()).isEqualTo("line one\nline two");
        assertThat(code.code()).doesNotContain("\r");
        assertThat(header(rendered.toPdfBytes())).isEqualTo("%PDF-");
    }

    @Test
    void rendersCodeWithBundledJetBrainsMonoFont() throws Exception {
        // Opt-in rich fonts: registers JetBrains Mono (from graph-compose-fonts, on the
        // test classpath) and switches the code font to it.
        MarkdownTheme theme = BundledFonts.jetBrainsMonoCode(DefaultMarkdownTheme.light());

        byte[] pdf = MarkdownComposer.create(theme).render("```java\nint x = 42;\n```").toPdfBytes();

        assertThat(theme.fontFamilies()).isNotEmpty();
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
    void rendersFootnotesToPdf() throws Exception {
        MarkdownComposer.Rendered rendered = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("A claim worth a note.[^1]\n\n[^1]: The supporting evidence.");

        assertThat(rendered.document().blocks()).anySatisfy(n -> assertThat(n).isInstanceOf(FootnotesNode.class));
        assertThat(header(rendered.toPdfBytes())).isEqualTo("%PDF-");
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

    @Test
    void nullMarkdownIsTreatedAsEmptyForBothBuilderConfigs() throws Exception {
        // render(String) documents "null is treated as empty"; hold that contract for both the
        // default (custom blocks on) and the custom-blocks-off path, which take different routes.
        byte[] withCustomBlocks = MarkdownComposer.builder().theme(DefaultMarkdownTheme.light())
                .build().render((String) null).toPdfBytes();
        byte[] withoutCustomBlocks = MarkdownComposer.builder().theme(DefaultMarkdownTheme.light())
                .customBlocks(false).build().render((String) null).toPdfBytes();

        assertThat(header(withCustomBlocks)).isEqualTo("%PDF-");
        assertThat(header(withoutCustomBlocks)).isEqualTo("%PDF-");
    }

    @Test
    void customBlocksOffLeavesTripleColonAsLiteralText() {
        // With custom blocks disabled, ':::' is not a fence — it stays ordinary paragraph text.
        MarkdownDocument doc = MarkdownComposer.builder().theme(DefaultMarkdownTheme.light())
                .customBlocks(false).build()
                .render(":::callout warning\nBe careful.\n:::").document();

        assertThat(doc.blocks()).noneMatch(CustomBlockNode.class::isInstance);
        boolean tripleColonSurvivesAsText = doc.blocks().stream()
                .filter(ParagraphNode.class::isInstance)
                .map(ParagraphNode.class::cast)
                .anyMatch(p -> plain(p.content()).contains(":::"));
        assertThat(tripleColonSurvivesAsText).isTrue();
    }

    @Test
    void nonStringRenderOverloadsRejectNull() {
        MarkdownComposer composer = MarkdownComposer.create(DefaultMarkdownTheme.light());
        assertThatThrownBy(() -> composer.render((com.vladsch.flexmark.util.ast.Document) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> composer.render((MarkdownDocument) null))
                .isInstanceOf(NullPointerException.class);
    }

    /** Concatenates the literal text of top-level inline runs (enough for these assertions). */
    private static String plain(List<InlineNode> runs) {
        StringBuilder sb = new StringBuilder();
        for (InlineNode run : runs) {
            if (run instanceof TextRun text) {
                sb.append(text.text());
            }
        }
        return sb.toString();
    }
}
