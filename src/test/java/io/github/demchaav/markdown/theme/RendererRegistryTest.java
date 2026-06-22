package io.github.demchaav.markdown.theme;

import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.inline.TextRun;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RendererRegistryTest {

    @Test
    void dispatchingANodeWithNoRegisteredRendererFailsFast() {
        // An empty registry knows no node types — dispatching one must fail loudly (naming the
        // class) rather than silently dropping the content. The null lookup throws before the
        // host/ctx are touched, so passing null for them is fine here.
        RendererRegistry empty = new RendererRegistry();
        MarkdownNode node = new ParagraphNode(List.of(new TextRun("dropped?")));

        assertThatThrownBy(() -> empty.render(node, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ParagraphNode");
    }
}
