package io.github.demchaav.markdown.mapper;

import io.github.demchaav.markdown.model.CodeBlockNode;
import io.github.demchaav.markdown.model.ColumnAlignment;
import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.model.ImageNode;
import io.github.demchaav.markdown.model.ListNode;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.model.ParagraphNode;
import io.github.demchaav.markdown.model.QuoteNode;
import io.github.demchaav.markdown.model.TableNode;
import io.github.demchaav.markdown.model.ThematicBreakNode;
import io.github.demchaav.markdown.model.inline.CodeRun;
import io.github.demchaav.markdown.model.inline.EmphasisRun;
import io.github.demchaav.markdown.model.inline.ImageRun;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.model.inline.StrikethroughRun;
import io.github.demchaav.markdown.model.inline.StrongRun;
import io.github.demchaav.markdown.model.inline.TextRun;
import io.github.demchaav.markdown.parser.FlexmarkMarkdownParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlexmarkAstMapperTest {

    private final FlexmarkMarkdownParser parser = new FlexmarkMarkdownParser();
    private final FlexmarkAstMapper mapper = new FlexmarkAstMapper();

    private MarkdownDocument parse(String markdown) {
        return mapper.map(parser.parse(markdown));
    }

    @Test
    void mapsHeadingLevelsAndText() {
        MarkdownDocument doc = parse("# Title\n\n### Subsection");

        assertThat(doc.blocks()).hasSize(2);
        HeadingNode h1 = (HeadingNode) doc.blocks().get(0);
        HeadingNode h3 = (HeadingNode) doc.blocks().get(1);
        assertThat(h1.level()).isEqualTo(1);
        assertThat(plain(h1.content())).isEqualTo("Title");
        assertThat(h3.level()).isEqualTo(3);
        assertThat(plain(h3.content())).isEqualTo("Subsection");
    }

    @Test
    void mapsInlineEmphasisCodeAndStrikethrough() {
        MarkdownDocument doc = parse("Hello **world** and *you* with `code` and ~~gone~~.");

        ParagraphNode p = (ParagraphNode) doc.blocks().get(0);
        List<InlineNode> runs = p.content();

        assertThat(runs.get(0)).isInstanceOf(TextRun.class);
        assertThat(runs).anySatisfy(n -> assertThat(n).isInstanceOf(StrongRun.class));
        assertThat(runs).anySatisfy(n -> assertThat(n).isInstanceOf(EmphasisRun.class));
        assertThat(runs).anySatisfy(n -> assertThat(n).isInstanceOf(CodeRun.class));
        assertThat(runs).anySatisfy(n -> assertThat(n).isInstanceOf(StrikethroughRun.class));

        StrongRun strong = (StrongRun) runs.stream().filter(StrongRun.class::isInstance).findFirst().orElseThrow();
        assertThat(plain(strong.children())).isEqualTo("world");
        CodeRun code = (CodeRun) runs.stream().filter(CodeRun.class::isInstance).findFirst().orElseThrow();
        assertThat(code.text()).isEqualTo("code");
    }

    @Test
    void mapsLinkWithUrlAndLabel() {
        MarkdownDocument doc = parse("See [the engine](https://github.com/DemchaAV/GraphCompose).");

        ParagraphNode p = (ParagraphNode) doc.blocks().get(0);
        LinkRun link = (LinkRun) p.content().stream().filter(LinkRun.class::isInstance).findFirst().orElseThrow();
        assertThat(link.url()).isEqualTo("https://github.com/DemchaAV/GraphCompose");
        assertThat(plain(link.children())).isEqualTo("the engine");
    }

    @Test
    void mapsUnorderedAndOrderedLists() {
        MarkdownDocument bullets = parse("- one\n- two\n- three");
        ListNode bulletList = (ListNode) bullets.blocks().get(0);
        assertThat(bulletList.ordered()).isFalse();
        assertThat(bulletList.items()).hasSize(3);

        MarkdownDocument numbered = parse("3. third\n4. fourth");
        ListNode orderedList = (ListNode) numbered.blocks().get(0);
        assertThat(orderedList.ordered()).isTrue();
        assertThat(orderedList.startNumber()).isEqualTo(3);
        assertThat(orderedList.items()).hasSize(2);
    }

    @Test
    void mapsNestedListAsBlockChildOfItem() {
        MarkdownDocument doc = parse("- outer\n    - inner");

        ListNode outer = (ListNode) doc.blocks().get(0);
        assertThat(outer.items()).hasSize(1);
        List<MarkdownNode> itemContent = outer.items().get(0).content();
        assertThat(itemContent.get(0)).isInstanceOf(ParagraphNode.class);
        assertThat(itemContent).anySatisfy(n -> assertThat(n).isInstanceOf(ListNode.class));
    }

    @Test
    void mapsFencedCodeBlockWithLanguage() {
        MarkdownDocument doc = parse("```java\nint x = 1;\nSystem.out.println(x);\n```");

        CodeBlockNode code = (CodeBlockNode) doc.blocks().get(0);
        assertThat(code.language()).isEqualTo("java");
        assertThat(code.code()).isEqualTo("int x = 1;\nSystem.out.println(x);");
    }

    @Test
    void mapsBlockquoteAndThematicBreak() {
        MarkdownDocument doc = parse("> quoted line\n\n---");

        QuoteNode quote = (QuoteNode) doc.blocks().get(0);
        assertThat(quote.content().get(0)).isInstanceOf(ParagraphNode.class);
        assertThat(plain(((ParagraphNode) quote.content().get(0)).content())).isEqualTo("quoted line");
        assertThat(doc.blocks().get(1)).isInstanceOf(ThematicBreakNode.class);
    }

    @Test
    void promotesStandaloneImageToBlockButKeepsInlineImageAsRun() {
        MarkdownDocument block = parse("![logo](logo.png \"Logo\")");
        ImageNode image = (ImageNode) block.blocks().get(0);
        assertThat(image.source()).isEqualTo("logo.png");
        assertThat(image.alt()).isEqualTo("logo");
        assertThat(image.title()).isEqualTo("Logo");

        MarkdownDocument inline = parse("text ![icon](icon.png) more");
        ParagraphNode p = (ParagraphNode) inline.blocks().get(0);
        assertThat(p.content()).anySatisfy(n -> assertThat(n).isInstanceOf(ImageRun.class));
    }

    @Test
    void mapsTableWithAlignmentsHeaderAndRows() {
        MarkdownDocument doc = parse("""
                | Name | Score |
                |:-----|------:|
                | Ann  | 10    |
                | Bob  | 7     |
                """);

        TableNode table = (TableNode) doc.blocks().get(0);
        assertThat(table.columnCount()).isEqualTo(2);
        assertThat(table.alignments()).containsExactly(ColumnAlignment.LEFT, ColumnAlignment.RIGHT);
        assertThat(plain(table.header().get(0).content())).isEqualTo("Name");
        assertThat(plain(table.header().get(1).content())).isEqualTo("Score");
        assertThat(table.bodyRows()).hasSize(2);
        assertThat(plain(table.bodyRows().get(0).get(0).content())).isEqualTo("Ann");
        assertThat(plain(table.bodyRows().get(1).get(1).content())).isEqualTo("7");
    }

    /** Flattens the literal text of a list of inline runs, descending into decorations. */
    private static String plain(List<InlineNode> runs) {
        StringBuilder sb = new StringBuilder();
        for (InlineNode run : runs) {
            if (run instanceof TextRun t) {
                sb.append(t.text());
            } else if (run instanceof CodeRun c) {
                sb.append(c.text());
            } else if (run instanceof StrongRun s) {
                sb.append(plain(s.children()));
            } else if (run instanceof EmphasisRun e) {
                sb.append(plain(e.children()));
            } else if (run instanceof StrikethroughRun s) {
                sb.append(plain(s.children()));
            } else if (run instanceof LinkRun l) {
                sb.append(plain(l.children()));
            }
            // breaks and images are ignored for plain-text comparison
        }
        return sb.toString();
    }
}
