package io.github.demchaav.markdown.model.inline;

/**
 * An inline (span-level) element inside a block — text and its decorations.
 *
 * <p>Inline nodes form a small tree: decoration runs ({@link StrongRun},
 * {@link EmphasisRun}, {@link StrikethroughRun}, {@link LinkRun}) wrap further
 * inline children, while {@link TextRun}, {@link CodeRun}, {@link ImageRun} and
 * {@link LineBreakRun} are leaves. The tree is preserved faithfully; flattening
 * the nested decorations into styled runs is the renderer's job, not the
 * model's. The model carries no GraphCompose types, so it can be tested and
 * reused independently of any rendering backend.</p>
 */
public sealed interface InlineNode
        permits TextRun, CodeRun, StrongRun, EmphasisRun, StrikethroughRun,
                LinkRun, ImageRun, LineBreakRun {
}
