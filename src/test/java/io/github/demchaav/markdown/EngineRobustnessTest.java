package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.composer.UnsupportedMarkdownException;
import io.github.demchaav.markdown.model.CodeBlockNode;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.UnsupportedBlockNode;
import io.github.demchaav.markdown.model.inline.UnsupportedInlineRun;
import io.github.demchaav.markdown.render.NodeRenderer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Hardening tests for the document engine — the things that must hold before a
 * stable public release: the {@code :::} scanner respects code fences, unsupported
 * content is surfaced rather than silently lost, and a built theme is immutable.
 */
class EngineRobustnessTest {

    private static MarkdownDocument model(String markdown) {
        return MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).document();
    }

    @Test
    void tripleColonInsideAFencedCodeBlockIsNotACustomBlock() {
        String md = "```text\n:::note\nnot a real custom block\n:::\n```";

        MarkdownDocument doc = model(md);

        assertThat(doc.blocks()).noneMatch(CustomBlockNode.class::isInstance);
        assertThat(doc.blocks()).anyMatch(CodeBlockNode.class::isInstance);
        CodeBlockNode code = (CodeBlockNode) doc.blocks().stream()
                .filter(CodeBlockNode.class::isInstance).findFirst().orElseThrow();
        assertThat(code.code()).contains(":::note").contains("not a real custom block");
    }

    @Test
    void tildeFencedCodeBlockAlsoShieldsTripleColon() {
        String md = "~~~\n:::warning\nstill code\n:::\n~~~";

        MarkdownDocument doc = model(md);

        assertThat(doc.blocks()).noneMatch(CustomBlockNode.class::isInstance);
        assertThat(doc.blocks()).anyMatch(CodeBlockNode.class::isInstance);
    }

    @Test
    void realCustomBlockOutsideCodeStillParses() {
        String md = ":::note\nA genuine custom block.\n:::";

        MarkdownDocument doc = model(md);

        assertThat(doc.blocks()).anyMatch(CustomBlockNode.class::isInstance);
    }

    @Test
    void aBuiltThemeRegistryIsImmutable() {
        MarkdownTheme theme = DefaultMarkdownTheme.light();
        NodeRenderer<ParagraphNode> noop = (node, host, ctx) -> { };

        assertThatThrownBy(() -> theme.registry().register(ParagraphNode.class, noop))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void derivingFromABuiltThemeStillAllowsOverrides() {
        MarkdownTheme base = DefaultMarkdownTheme.light();
        NodeRenderer<ParagraphNode> noop = (node, host, ctx) -> { };

        // builder(base) copies into a fresh mutable registry, so overriding works even
        // though the base theme's own registry is frozen.
        MarkdownTheme derived = MarkdownTheme.builder(base).renderer(ParagraphNode.class, noop).build();

        assertThat(derived.registry().hasRenderer(ParagraphNode.class)).isTrue();
    }

    @Test
    void relativeAndAnchorLinksDoNotCrashTheRender() {
        // The engine only annotates absolute-URI links; a schemeless href (anchor, relative,
        // root-relative) must render as link-styled text, not abort the whole document.
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("[home](#intro), [doc](readme.html), [up](../x), [abs](/p), [ok](https://example.com)")
                .toPdfBytes();

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void unsupportedHtmlBlockIsSurfacedNotDropped() {
        MarkdownDocument doc = model("<div class=\"note\">important content</div>");

        UnsupportedBlockNode unsupported = (UnsupportedBlockNode) doc.blocks().stream()
                .filter(UnsupportedBlockNode.class::isInstance).findFirst().orElseThrow();
        assertThat(unsupported.raw()).contains("important content");

        // and it renders (lenient default) rather than crashing
        byte[] pdf = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("<div>important content</div>").toPdfBytes();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void unsupportedInlineHtmlIsSurfacedNotDropped() {
        // <sub>/</sub> are not modelled; the raw tags must survive into the model, not vanish.
        MarkdownDocument doc = model("H<sub>2</sub>O");

        ParagraphNode paragraph = (ParagraphNode) doc.blocks().stream()
                .filter(ParagraphNode.class::isInstance).findFirst().orElseThrow();
        assertThat(paragraph.content()).anyMatch(UnsupportedInlineRun.class::isInstance);
    }

    @Test
    void strictModeRejectsAnUnsupportedHtmlBlock() {
        MarkdownComposer strict = MarkdownComposer.builder()
                .theme(DefaultMarkdownTheme.light()).strictMode(true).build();

        assertThatThrownBy(() -> strict.render("<div>important content</div>"))
                .isInstanceOf(UnsupportedMarkdownException.class)
                .hasMessageContaining("unsupported block");
    }

    @Test
    void strictModeRejectsUnsupportedInlineHtml() {
        MarkdownComposer strict = MarkdownComposer.builder()
                .theme(DefaultMarkdownTheme.light()).strictMode(true).build();

        assertThatThrownBy(() -> strict.render("text with <span>inline html</span>"))
                .isInstanceOf(UnsupportedMarkdownException.class)
                .hasMessageContaining("unsupported inline");
    }

    @Test
    void strictModeAcceptsBracketedTextAndUndefinedReferenceLinks() {
        MarkdownComposer strict = MarkdownComposer.builder()
                .theme(DefaultMarkdownTheme.light()).strictMode(true).build();

        // [TODO] and [text][ref] with no definition are valid CommonMark — rendered as
        // literal text, NOT unsupported content. Strict mode must not reject them.
        byte[] pdf = strict.render("A note [TODO] and a [broken][ref] link.").toPdfBytes();

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void strictModeAcceptsFullySupportedContent() {
        MarkdownComposer strict = MarkdownComposer.builder()
                .theme(DefaultMarkdownTheme.light()).strictMode(true).build();

        byte[] pdf = strict.render("# Heading\n\nText with **bold** and `code`.\n\n- a\n- b")
                .toPdfBytes();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
