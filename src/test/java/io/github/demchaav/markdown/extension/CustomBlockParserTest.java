package io.github.demchaav.markdown.extension;

import io.github.demchaav.markdown.mapper.FlexmarkAstMapper;
import io.github.demchaav.markdown.model.CustomBlockNode;
import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.parser.FlexmarkMarkdownParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomBlockParserTest {

    private final CustomBlockParser parser =
            new CustomBlockParser(new FlexmarkMarkdownParser(), new FlexmarkAstMapper());

    @Test
    void extractsCalloutWithTypeAndVariantAroundNormalText() {
        MarkdownDocument doc = parser.parse("""
                # Title

                :::callout warning
                Be careful here.
                :::

                After.
                """);

        assertThat(doc.blocks()).hasSize(3);
        assertThat(doc.blocks().get(0)).isInstanceOf(HeadingNode.class);

        CustomBlockNode callout = (CustomBlockNode) doc.blocks().get(1);
        assertThat(callout.type()).isEqualTo("callout");
        assertThat(callout.variant()).isEqualTo("warning");
        assertThat(callout.content().get(0)).isInstanceOf(ParagraphNode.class);

        assertThat(doc.blocks().get(2)).isInstanceOf(ParagraphNode.class);
    }

    @Test
    void supportsNestedCustomBlocks() {
        MarkdownDocument doc = parser.parse("""
                :::callout
                outer text

                :::note info
                inner text
                :::
                :::
                """);

        CustomBlockNode outer = (CustomBlockNode) doc.blocks().get(0);
        assertThat(outer.type()).isEqualTo("callout");
        assertThat(outer.variant()).isNull();
        assertThat(outer.content()).anySatisfy(n -> assertThat(n).isInstanceOf(CustomBlockNode.class));

        CustomBlockNode inner = (CustomBlockNode) outer.content().stream()
                .filter(CustomBlockNode.class::isInstance).findFirst().orElseThrow();
        assertThat(inner.type()).isEqualTo("note");
        assertThat(inner.variant()).isEqualTo("info");
    }

    @Test
    void passesThroughPlainMarkdownUnchanged() {
        MarkdownDocument doc = parser.parse("Just a paragraph.\n\nAnd another.");

        assertThat(doc.blocks()).hasSize(2);
        assertThat(doc.blocks()).allSatisfy(n -> assertThat(n).isInstanceOf(ParagraphNode.class));
    }
}
