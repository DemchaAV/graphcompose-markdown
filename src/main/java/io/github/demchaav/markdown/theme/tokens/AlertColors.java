package io.github.demchaav.markdown.theme.tokens;

import com.demcha.compose.document.style.DocumentColor;

import java.util.Objects;

/**
 * Per-variant accent colours for GitHub-style alerts ({@code > [!NOTE]}, …) and for
 * {@code :::} custom-block callouts. These were previously hard-coded inside the renderers;
 * pulling them into a token group lets a theme retune them — e.g. so a dark theme can pick
 * accents that read well on a dark surface — without replacing the alert/callout renderers.
 *
 * <p>The GitHub-alert accents ({@link #note()} … {@link #caution()}) and the named
 * {@code :::} callout accents ({@link #calloutInfo()} … {@link #calloutSuccess()}) are kept
 * as separate fields because the two surfaces ship deliberately different palettes. A
 * {@code :::} block with an unknown (or absent) variant is not covered here — it falls back
 * to the theme's general {@code ColorTokens.accent()} in the renderer.</p>
 *
 * @param note           accent for {@code [!NOTE]}
 * @param tip            accent for {@code [!TIP]}
 * @param important      accent for {@code [!IMPORTANT]}
 * @param warning        accent for {@code [!WARNING]}
 * @param caution        accent for {@code [!CAUTION]}
 * @param calloutInfo    accent for {@code :::info} / {@code :::note}
 * @param calloutWarning accent for {@code :::warning} / {@code :::caution}
 * @param calloutError   accent for {@code :::error} / {@code :::danger}
 * @param calloutSuccess accent for {@code :::success} / {@code :::tip}
 */
public record AlertColors(
        DocumentColor note,
        DocumentColor tip,
        DocumentColor important,
        DocumentColor warning,
        DocumentColor caution,
        DocumentColor calloutInfo,
        DocumentColor calloutWarning,
        DocumentColor calloutError,
        DocumentColor calloutSuccess) {

    /** Validates every accent is present. */
    public AlertColors {
        Objects.requireNonNull(note, "note");
        Objects.requireNonNull(tip, "tip");
        Objects.requireNonNull(important, "important");
        Objects.requireNonNull(warning, "warning");
        Objects.requireNonNull(caution, "caution");
        Objects.requireNonNull(calloutInfo, "calloutInfo");
        Objects.requireNonNull(calloutWarning, "calloutWarning");
        Objects.requireNonNull(calloutError, "calloutError");
        Objects.requireNonNull(calloutSuccess, "calloutSuccess");
    }

    /**
     * The default palette — the GitHub-ish accents the renderers used before this was a token
     * group, so an existing theme renders identically.
     *
     * @return the default alert/callout palette
     */
    public static AlertColors defaultPalette() {
        return new AlertColors(
                DocumentColor.rgb(9, 105, 218),    // note — blue
                DocumentColor.rgb(26, 127, 55),    // tip — green
                DocumentColor.rgb(130, 80, 223),   // important — purple
                DocumentColor.rgb(154, 103, 0),    // warning — amber
                DocumentColor.rgb(207, 34, 46),    // caution — red
                DocumentColor.rgb(37, 99, 235),    // callout info / note
                DocumentColor.rgb(217, 119, 6),    // callout warning / caution
                DocumentColor.rgb(220, 38, 38),    // callout error / danger
                DocumentColor.rgb(22, 163, 74));   // callout success / tip
    }
}
