package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.node.InlineRun;
import com.demcha.compose.document.node.InlineShapeRun;
import com.demcha.compose.document.node.InlineTextRun;
import com.demcha.compose.document.style.DocumentTextStyle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Geometric emoji become inline vector shapes (so they survive in PDF instead of a
 * missing-glyph "?"); everything else is left as text.
 */
class EmojiShapesTest {

    private static final DocumentTextStyle STYLE = DocumentTextStyle.DEFAULT;

    @Test
    void detectsMappableEmojiOnly() {
        assertThat(EmojiShapes.contains("a 🔴 b")).isTrue();   // red circle
        assertThat(EmojiShapes.contains("🟩 done")).isTrue();   // green square
        assertThat(EmojiShapes.contains("⭐ top")).isTrue();    // star
        assertThat(EmojiShapes.contains("plain text")).isFalse();
        assertThat(EmojiShapes.contains("smiley 😀 rocket 🚀")).isFalse(); // non-geometric: untouched
    }

    @Test
    void replacesEmojiWithShapeRunsAroundText() {
        RichText rich = RichText.empty();
        EmojiShapes.append(rich, "🔴 blocker", STYLE, 12.0);

        List<InlineRun> runs = rich.runs();
        assertThat(runs).hasSize(2);
        assertThat(runs.get(0)).isInstanceOf(InlineShapeRun.class);            // the dot
        assertThat(runs.get(1)).isInstanceOf(InlineTextRun.class);             // " blocker"
        assertThat(((InlineTextRun) runs.get(1)).text()).isEqualTo(" blocker");
    }

    @Test
    void interleavesMultipleEmoji() {
        RichText rich = RichText.empty();
        EmojiShapes.append(rich, "🔴 a 🟢 b", STYLE, 12.0);
        // shape, " a ", shape, " b"
        List<InlineRun> runs = rich.runs();
        assertThat(runs).hasSize(4);
        assertThat(runs.get(0)).isInstanceOf(InlineShapeRun.class);
        assertThat(runs.get(2)).isInstanceOf(InlineShapeRun.class);
    }

    @Test
    void plainTextStaysASingleTextRun() {
        RichText rich = RichText.empty();
        EmojiShapes.append(rich, "no emoji here", STYLE, 12.0);
        assertThat(rich.runs()).hasSize(1);
        assertThat(rich.runs().get(0)).isInstanceOf(InlineTextRun.class);
    }
}
