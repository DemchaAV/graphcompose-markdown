package io.github.demchaav.markdown.render;

import io.github.demchaav.markdown.theme.RendererRegistry;

/**
 * A reusable bundle of node renderers — the unit of composition for rendering
 * behaviour.
 *
 * <p>A pack registers one or more {@link NodeRenderer}s (and/or custom-block
 * renderers) into a {@link RendererRegistry}. Themes compose packs:
 * {@code MarkdownTheme.builder(base).pack(new StandardPack()).pack(myPack)}, so a
 * project can ship its own pack of node renderers and drop it onto any theme,
 * reusing everyone else's renderers and overriding only what it needs.</p>
 */
@FunctionalInterface
public interface RendererPack {

    /**
     * Registers this pack's renderers into the registry. Later registrations win,
     * so packs added afterwards override earlier ones for the same node type.
     *
     * @param registry the registry to populate
     */
    void registerInto(RendererRegistry registry);
}
