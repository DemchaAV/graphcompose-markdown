package io.github.demchaav.markdown.render;

import io.github.demchaav.markdown.model.inline.FootnoteRefRun;
import io.github.demchaav.markdown.model.inline.TextRun;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Per-document anchor bookkeeping: duplicate-heading de-duplication and footnote back-anchors. */
class RenderStateTest {

    @Test
    void duplicateHeadingTextGetsSuffixedAnchors() {
        RenderState state = new RenderState();
        assertThat(state.headingAnchor("Overview")).isEqualTo("overview");
        assertThat(state.headingAnchor("Overview")).isEqualTo("overview-1");
        assertThat(state.headingAnchor("Overview")).isEqualTo("overview-2");
        assertThat(state.headingAnchor("Details")).isEqualTo("details");
    }

    @Test
    void emptyHeadingTextFallsBackToSection() {
        RenderState state = new RenderState();
        assertThat(state.headingAnchor("###")).isEqualTo("section");
        assertThat(state.headingAnchor("")).isEqualTo("section-1");
    }

    @Test
    void footnoteBackAnchorIsPlacedOncePerNumber() {
        RenderState state = new RenderState();
        assertThat(state.footnoteBackAnchor(List.of(new TextRun("x"), new FootnoteRefRun(1)))).isEqualTo("fnref-1");
        // A later block citing the same footnote gets no anchor — the back-link already has a home.
        assertThat(state.footnoteBackAnchor(List.of(new FootnoteRefRun(1)))).isNull();
        assertThat(state.footnoteBackAnchor(List.of(new FootnoteRefRun(2)))).isEqualTo("fnref-2");
        assertThat(state.footnoteBackAnchor(List.of(new TextRun("no refs")))).isNull();
    }
}
