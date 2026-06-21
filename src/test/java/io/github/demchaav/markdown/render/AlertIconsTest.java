package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.model.AlertType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every alert type ships a parseable vector icon. This guards the bundled SVG
 * resources (presence + parseability via the engine's {@code SvgIcon}) so a
 * missing or broken icon file is caught at build time rather than silently
 * degrading to a glyph-less title at render time.
 */
class AlertIconsTest {

    @Test
    void everyAlertTypeAppendsAVectorGlyph() {
        for (AlertType type : AlertType.values()) {
            RichText rich = RichText.empty();
            boolean appended = AlertIcons.append(rich, type, DocumentColor.rgb(10, 20, 30), 12.0);
            assertThat(appended).as("icon available for %s", type).isTrue();
            assertThat(rich.runs()).as("a glyph run was added for %s", type).isNotEmpty();
        }
    }

    @Test
    void nonPositiveOrNonFiniteSizeDegradesInsteadOfThrowing() {
        // A theme may carry a 0 / negative / NaN body size (the engine tolerates it elsewhere);
        // the icon must drop out, not crash the alert render.
        DocumentColor c = DocumentColor.rgb(10, 20, 30);
        for (double size : new double[]{0.0, -3.0, Double.NaN, Double.POSITIVE_INFINITY}) {
            RichText rich = RichText.empty();
            assertThat(AlertIcons.append(rich, AlertType.WARNING, c, size))
                    .as("size %s degrades", size).isFalse();
            assertThat(rich.runs()).as("no run added for size %s", size).isEmpty();
        }
    }
}
