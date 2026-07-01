package io.github.demchaav.markdown.render;

import io.github.demchaav.markdown.model.HeadingNode;
import io.github.demchaav.markdown.model.inline.EmphasisRun;
import io.github.demchaav.markdown.model.inline.FootnoteRefRun;
import io.github.demchaav.markdown.model.inline.InlineNode;
import io.github.demchaav.markdown.model.inline.LinkRun;
import io.github.demchaav.markdown.model.inline.StrikethroughRun;
import io.github.demchaav.markdown.model.inline.StrongRun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mutable, per-render bookkeeping shared across every {@link RenderContext}
 * derived during one document render (a {@code withTextColor(...)} child keeps
 * the same state, so anchor numbering stays document-global).
 *
 * <p>It holds the two things that must be unique across the whole document:
 * de-duplicated heading anchor slugs, and the set of footnote numbers whose
 * back-reference anchor has already been placed.</p>
 */
final class RenderState {

    /** Base slug -> number of times it has been assigned, for GitHub-style de-duplication. */
    private final Map<String, Integer> slugCounts = new HashMap<>();

    /** Footnote numbers whose {@code fnref-N} back-anchor has already been placed. */
    private final Set<Integer> placedFootnoteRefs = new HashSet<>();

    /** Heading node -> its assigned slug, planned once up front so a [TOC] above the headings agrees. */
    private final Map<HeadingNode, String> headingSlugs = new IdentityHashMap<>();

    /** Every heading in document order, for an auto-generated table of contents. */
    private final List<TocEntry> tocEntries = new ArrayList<>();

    /** A heading paired with its already-flattened plain text, fed to {@link #planHeadings}. */
    record HeadingRef(HeadingNode node, String text) {
    }

    /**
     * Assigns a slug to every heading in document order (the same de-duplicating scheme as
     * {@link #headingAnchor}) and records the ordered table-of-contents entries. Run once before
     * rendering so a {@code [TOC]} that appears above its headings resolves to the same slugs the
     * headings declare. Headings with empty text still get a slug (so duplicates stay distinct) but
     * are left out of the TOC by the renderer.
     *
     * @param refs every heading and its plain text, in document order
     */
    void planHeadings(List<HeadingRef> refs) {
        for (HeadingRef ref : refs) {
            String slug = headingAnchor(ref.text());
            headingSlugs.put(ref.node(), slug);
            tocEntries.add(new TocEntry(ref.node().level(), ref.text(), slug));
        }
    }

    /** @return the planned slug for a heading, or {@code null} if it was not planned */
    String slugFor(HeadingNode node) {
        return headingSlugs.get(node);
    }

    /** @return the document's headings as ordered table-of-contents entries (empty before planning) */
    List<TocEntry> tocEntries() {
        return tocEntries;
    }

    /**
     * Returns a unique anchor slug for a heading, matching GitHub's scheme: the
     * first occurrence of a slug is bare, later duplicates get a {@code -1},
     * {@code -2}, … suffix. A heading with no slug-able text gets {@code section}.
     *
     * @param title the heading's plain text
     * @return a document-unique anchor name (never blank)
     */
    String headingAnchor(String title) {
        String base = Slugs.slugify(title);
        if (base.isEmpty()) {
            base = "section";
        }
        Integer seen = slugCounts.get(base);
        if (seen == null) {
            slugCounts.put(base, 0);
            return base;
        }
        int next = seen + 1;
        slugCounts.put(base, next);
        return base + "-" + next;
    }

    /**
     * Returns the back-reference anchor ({@code fnref-N}) for the first footnote
     * referenced in {@code content} whose anchor has not yet been placed, marking
     * it placed; or {@code null} if the content references no fresh footnote.
     *
     * <p>One anchor is placed per footnote, at its first referencing block (body
     * paragraph, list item, or table cell), so the note's back-link lands on the
     * citation. A footnote referenced only inside a heading keeps its forward link
     * but no back-link — a heading's single anchor slot already carries its slug.</p>
     *
     * @param content the inline content of a block
     * @return the {@code fnref-N} anchor to place, or {@code null}
     */
    String footnoteBackAnchor(List<InlineNode> content) {
        int number = firstFreshFootnote(content);
        if (number < 0) {
            return null;
        }
        placedFootnoteRefs.add(number);
        return "fnref-" + number;
    }

    /** @return the number of the first not-yet-placed footnote referenced in {@code nodes}, or {@code -1} */
    private int firstFreshFootnote(List<InlineNode> nodes) {
        for (InlineNode node : nodes) {
            if (node instanceof FootnoteRefRun ref) {
                if (!placedFootnoteRefs.contains(ref.number())) {
                    return ref.number();
                }
            } else if (node instanceof StrongRun strong) {
                int nested = firstFreshFootnote(strong.children());
                if (nested >= 0) {
                    return nested;
                }
            } else if (node instanceof EmphasisRun emphasis) {
                int nested = firstFreshFootnote(emphasis.children());
                if (nested >= 0) {
                    return nested;
                }
            } else if (node instanceof StrikethroughRun strike) {
                int nested = firstFreshFootnote(strike.children());
                if (nested >= 0) {
                    return nested;
                }
            } else if (node instanceof LinkRun link) {
                int nested = firstFreshFootnote(link.children());
                if (nested >= 0) {
                    return nested;
                }
            }
        }
        return -1;
    }
}
