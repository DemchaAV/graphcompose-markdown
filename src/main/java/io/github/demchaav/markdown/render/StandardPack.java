package io.github.demchaav.markdown.render;

import io.github.demchaav.markdown.theme.RendererRegistry;

/**
 * The built-in renderer pack: renderers for every standard Markdown node type
 * (headings, paragraphs, lists, code blocks, quotes, rules, images, tables,
 * footnotes) plus the default {@code :::} custom-block dispatcher.
 *
 * <p>This is what {@link io.github.demchaav.markdown.theme.DefaultMarkdownTheme}
 * registers. Compose your own pack on top to add or override renderers.</p>
 */
public final class StandardPack implements RendererPack {

    @Override
    public void registerInto(RendererRegistry registry) {
        BuiltinRenderers.registerDefaults(registry);
    }
}
