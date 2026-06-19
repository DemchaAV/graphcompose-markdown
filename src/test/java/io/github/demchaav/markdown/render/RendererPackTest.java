package io.github.demchaav.markdown.render;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RendererPackTest {

    private static String header(byte[] pdf) {
        return new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.US_ASCII);
    }

    @Test
    void customBlockTypeDispatchUsesRegisteredRendererAndFallsBackOtherwise() throws Exception {
        AtomicBoolean chartRendered = new AtomicBoolean(false);
        NodeRenderer<CustomBlockNode> chart = (node, host, ctx) -> {
            chartRendered.set(true);
            host.addParagraph(p -> p.text("CHART: " + node.type()));
        };

        // Reuse every standard renderer; add a renderer for the project's own ::: type.
        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .customBlock("chart", chart)
                .build();

        byte[] pdf = MarkdownComposer.create(theme)
                .render(":::chart\ndata\n:::\n\n:::callout warning\nheads up\n:::")
                .toPdfBytes();

        assertThat(chartRendered).as("the :::chart renderer is invoked").isTrue();
        assertThat(header(pdf)).isEqualTo("%PDF-"); // the :::callout still renders via the default
    }

    @Test
    void customBlockRendererWorksOnAThemeBuiltFromScratch() throws Exception {
        AtomicBoolean rendered = new AtomicBoolean(false);
        NodeRenderer<CustomBlockNode> chart = (node, host, ctx) -> {
            rendered.set(true);
            host.addParagraph(p -> p.text("CHART"));
        };

        // No StandardPack and no pre-registered dispatcher: customBlock(...) must still
        // wire the dispatcher so the renderer is actually reached.
        MarkdownTheme theme = MarkdownTheme.builder()
                .tokens(DefaultMarkdownTheme.light().tokens())
                .customBlock("chart", chart)
                .build();

        byte[] pdf = MarkdownComposer.create(theme).render(":::chart\ndata\n:::").toPdfBytes();

        assertThat(rendered).as("custom block renders without StandardPack").isTrue();
        assertThat(header(pdf)).isEqualTo("%PDF-");
    }

    @Test
    void rendererPackComposesIntoATheme() throws Exception {
        AtomicInteger applied = new AtomicInteger();
        RendererPack metricPack = registry -> {
            applied.incrementAndGet();
            registry.registerCustomBlock("metric",
                    (node, host, ctx) -> host.addParagraph(p -> p.text("METRIC")));
        };

        MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
                .pack(metricPack)
                .build();

        assertThat(applied.get()).isEqualTo(1);
        byte[] pdf = MarkdownComposer.create(theme).render(":::metric\n42\n:::").toPdfBytes();
        assertThat(header(pdf)).isEqualTo("%PDF-");
    }
}
