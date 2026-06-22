package io.github.demchaav.markdown;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.model.ListNode;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CommonMark distinguishes loose lists (items separated by blank lines) from tight ones. The
 * mapper now carries that distinction so the renderer can space loose items further apart.
 */
class ListLoosenessTest {

    private static ListNode firstList(String markdown) {
        return (ListNode) MarkdownComposer.create(DefaultMarkdownTheme.light()).render(markdown).document()
                .blocks().stream().filter(ListNode.class::isInstance).findFirst().orElseThrow();
    }

    @Test
    void blankLinesBetweenItemsMakeTheListLoose() {
        assertThat(firstList("- one\n\n- two\n\n- three").loose()).isTrue();
    }

    @Test
    void adjacentItemsMakeTheListTight() {
        assertThat(firstList("- one\n- two\n- three").loose()).isFalse();
    }

    @Test
    void bothLooseAndTightListsRenderToPdf() {
        // Exercises both spacing branches in the renderer.
        byte[] loose = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("- one\n\n- two\n\n- three").toPdfBytes();
        byte[] tight = MarkdownComposer.create(DefaultMarkdownTheme.light())
                .render("- one\n- two\n- three").toPdfBytes();

        assertThat(new String(loose, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(new String(tight, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
