package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.node.InlineImageAlignment;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentPathSegment;
import com.demcha.compose.document.style.ShapeOutline;
import com.demcha.compose.document.svg.SvgIcon;
import io.github.demchaav.markdown.model.AlertType;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The GitHub-style alert icons, drawn inline next to the alert title.
 *
 * <p>Each icon's geometry comes from a small bundled SVG, parsed once via
 * {@link SvgIcon} into engine path segments and rendered through
 * {@link RichText#shape} as a <em>filled</em> glyph — so it sits on the title's
 * text baseline, scales with the type size, and is painted in the alert's accent
 * colour. It is true vector (no font glyph and no raster image), and recolouring
 * is free because the fill is applied here rather than baked into the SVG.</p>
 *
 * <p>The icons are solid silhouettes whose {@code "!"} / {@code "i"} marks are
 * cut out by the non-zero winding rule. Filled (not stroked) icons are used
 * deliberately: the engine's inline shape stroke carries no round line-cap, so a
 * thin outline icon's round-capped dots would render square — a solid silhouette
 * sidesteps that entirely.</p>
 *
 * <p>Built on the published engine's inline-shape API (the same family the list
 * bullets use). If an icon resource is missing or unparseable, the title simply
 * renders without a glyph — the alert never fails to render.</p>
 */
final class AlertIcons {

    /** Parsed, colour-independent geometry for one icon. */
    private record Glyph(List<DocumentPathSegment> segments, double aspectRatio) {
    }

    private static final Map<AlertType, Glyph> GLYPHS = load();

    private AlertIcons() {
    }

    /**
     * Appends the alert's icon to {@code rich}, sized to {@code size} points tall and
     * filled with {@code accent}.
     *
     * @return {@code true} if a glyph was appended; {@code false} when this alert has no
     * available icon (the caller then renders the title with no leading glyph)
     */
    static boolean append(RichText rich, AlertType type, DocumentColor accent, double size) {
        Glyph glyph = GLYPHS.get(type);
        if (glyph == null) {
            return false;
        }
        // A non-positive or non-finite size (e.g. a theme with bodySize 0/NaN, which the engine
        // tolerates elsewhere) would make ShapeOutline.path throw — degrade to a glyph-less title.
        if (!(size > 0.0) || Double.isInfinite(size)) {
            return false;
        }
        ShapeOutline outline = ShapeOutline.path(size * glyph.aspectRatio(), size, glyph.segments());
        rich.shape(outline, accent, null, InlineImageAlignment.CENTER, 0.0, null);
        return true;
    }

    private static Map<AlertType, Glyph> load() {
        Map<AlertType, Glyph> map = new EnumMap<>(AlertType.class);
        put(map, AlertType.NOTE, "alert-note.svg");
        put(map, AlertType.TIP, "alert-tip.svg");
        put(map, AlertType.IMPORTANT, "alert-important.svg");
        put(map, AlertType.WARNING, "alert-warning.svg");
        put(map, AlertType.CAUTION, "alert-caution.svg");
        return map;
    }

    private static void put(Map<AlertType, Glyph> map, AlertType type, String resource) {
        Glyph glyph = parse(resource);
        if (glyph != null) {
            map.put(type, glyph);
        }
    }

    private static Glyph parse(String resource) {
        try (InputStream in = AlertIcons.class.getResourceAsStream("icons/" + resource)) {
            if (in == null) {
                return null;
            }
            String svg = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            SvgIcon icon = SvgIcon.parse(svg);
            // Merge every layer's geometry into one filled outline.
            List<DocumentPathSegment> merged = new ArrayList<>();
            for (SvgIcon.Layer layer : icon.layers()) {
                merged.addAll(layer.geometry().segments());
            }
            if (merged.size() < 2) {
                return null;
            }
            return new Glyph(List.copyOf(merged), icon.aspectRatio());
        } catch (RuntimeException | java.io.IOException e) {
            return null; // degrade: render the title without an icon rather than failing
        }
    }
}
