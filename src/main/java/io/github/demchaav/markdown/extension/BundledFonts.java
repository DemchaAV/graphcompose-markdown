package io.github.demchaav.markdown.extension;

import com.demcha.compose.font.DefaultFonts;
import com.demcha.compose.font.FontFamilyDefinition;
import com.demcha.compose.font.FontName;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.tokens.FontFamily;
import io.github.demchaav.markdown.theme.tokens.MarkdownTokens;

/**
 * Opt-in helpers for the bundled fonts shipped by the {@code graph-compose-fonts}
 * artifact (JetBrains Mono, IBM Plex, …). The core themes use only the PDF base-14
 * fonts, so they need no font dependency; call these to upgrade a theme to a richer
 * code font.
 *
 * <p><strong>Requires {@code io.github.demchaav:graph-compose-fonts} on the runtime
 * classpath.</strong> This library declares that artifact {@code optional}, so a
 * consumer that calls {@code BundledFonts} must add the dependency themselves.
 * Without the font resources, {@link #jetBrainsMonoCode(MarkdownTheme)} throws
 * (the family is not found) rather than silently rendering the wrong font.</p>
 *
 * <pre>{@code
 * MarkdownTheme theme = BundledFonts.jetBrainsMonoCode(DefaultMarkdownTheme.light());
 * MarkdownComposer.create(theme).render(md).writePdf(out);
 * }</pre>
 */
public final class BundledFonts {

    private BundledFonts() {
    }

    /**
     * Returns a copy of {@code base} that renders code in <em>JetBrains Mono</em>:
     * the font family is registered into the render session and the code token
     * family is switched to {@link FontFamily#MONO_JETBRAINS}. Body and heading
     * fonts are left untouched.
     *
     * @param base the theme to upgrade
     * @return a new theme that renders code in JetBrains Mono
     * @throws IllegalStateException if the bundled font family is not on the classpath
     */
    public static MarkdownTheme jetBrainsMonoCode(MarkdownTheme base) {
        return withBundledCodeFont(base, FontName.JETBRAINS_MONO, FontFamily.MONO_JETBRAINS);
    }

    private static MarkdownTheme withBundledCodeFont(MarkdownTheme base, FontName font, FontFamily family) {
        FontFamilyDefinition definition = bundledFamily(font);
        MarkdownTokens tokens = base.tokens();
        MarkdownTokens richTokens = tokens.withTypography(tokens.typography().withCodeFamily(family));
        return MarkdownTheme.builder(base)
                .tokens(richTokens)
                .fontFamily(definition)
                .build();
    }

    private static FontFamilyDefinition bundledFamily(FontName font) {
        return DefaultFonts.googleFamilies().stream()
                .filter(def -> font.equals(def.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Bundled font '" + font.name() + "' was not found. Add the "
                                + "io.github.demchaav:graph-compose-fonts dependency to use BundledFonts."));
    }
}
