package io.github.demchaav.markdown.render;

import com.demcha.compose.document.dsl.RichText;
import com.demcha.compose.document.node.InlineImageAlignment;
import com.demcha.compose.document.style.DocumentColor;
import com.demcha.compose.document.style.DocumentStroke;
import com.demcha.compose.document.style.DocumentTextStyle;
import com.demcha.compose.document.style.ShapeOutline;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders a curated set of <em>geometric</em> Unicode emoji — coloured circles, squares,
 * triangles, diamonds and stars — as native inline vector shapes, in the emoji's
 * conventional colour.
 *
 * <p>PDF fonts carry no colour-emoji glyphs, so a literal {@code 🔴} would otherwise come
 * out as a missing-glyph {@code "?"}. These shapes map cleanly onto the engine's inline
 * vector primitives ({@code RichText.dot/triangle/diamond/star/shape} — the same family as
 * the list bullets), so they render crisply at any size and recolour nothing (an emoji's
 * colour is intrinsic). Non-geometric emoji ({@code 😀}, {@code 🚀}, …) are left untouched —
 * they still go through the {@code :shortcode:} image path or render as text.</p>
 *
 * <p>Applied only to flowing inline text; fenced/inline code stays verbatim (Markdown rule).</p>
 */
final class EmojiShapes {

    private enum Kind {CIRCLE, SQUARE, TRIANGLE, DIAMOND, STAR}

    /** One mappable emoji: its shape kind, fill, optional outline stroke colour, and size factor. */
    private record Glyph(Kind kind, DocumentColor fill, DocumentColor strokeColor, double scale) {
    }

    private static final Map<Integer, Glyph> GLYPHS = build();

    private EmojiShapes() {
    }

    /** @return {@code true} if {@code text} contains at least one mappable geometric emoji. */
    static boolean contains(String text) {
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            if (GLYPHS.containsKey(cp)) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    /**
     * Appends {@code text} to {@code rich}, replacing mappable geometric emoji with inline
     * vector shapes sized to {@code emSize} points; runs of other characters keep
     * {@code textStyle}.
     */
    static void append(RichText rich, String text, DocumentTextStyle textStyle, double emSize) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int cc = Character.charCount(cp);
            Glyph glyph = GLYPHS.get(cp);
            if (glyph != null) {
                if (buf.length() > 0) {
                    rich.style(buf.toString(), textStyle);
                    buf.setLength(0);
                }
                emit(rich, glyph, emSize);
            } else {
                buf.appendCodePoint(cp);
            }
            i += cc;
        }
        if (buf.length() > 0) {
            rich.style(buf.toString(), textStyle);
        }
    }

    private static void emit(RichText rich, Glyph g, double emSize) {
        double s = emSize * g.scale();
        DocumentStroke stroke = g.strokeColor() == null
                ? null : new DocumentStroke(g.strokeColor(), Math.max(0.4, s * 0.08));
        switch (g.kind()) {
            case CIRCLE -> {
                if (stroke != null) {
                    rich.dot(s, g.fill(), stroke);
                } else {
                    rich.dot(s, g.fill());
                }
            }
            case SQUARE -> rich.shape(new ShapeOutline.RoundedRectangle(s, s, s * 0.22),
                    g.fill(), stroke, InlineImageAlignment.CENTER, 0.0, null);
            case TRIANGLE -> rich.triangle(s, g.fill());
            case DIAMOND -> rich.diamond(s, g.fill());
            case STAR -> rich.star(s, g.fill());
        }
    }

    private static Map<Integer, Glyph> build() {
        Map<Integer, Glyph> m = new HashMap<>();
        // Coloured circles → dot. Colours follow the common (Twemoji-ish) emoji palette.
        circle(m, 0x1F534, 221, 46, 68);    // 🔴 red
        circle(m, 0x1F7E0, 244, 144, 12);   // 🟠 orange
        circle(m, 0x1F7E1, 253, 203, 88);   // 🟡 yellow
        circle(m, 0x1F7E2, 120, 177, 89);   // 🟢 green
        circle(m, 0x1F535, 85, 172, 238);   // 🔵 blue
        circle(m, 0x1F7E3, 170, 142, 214);  // 🟣 purple
        circle(m, 0x1F7E4, 193, 105, 79);   // 🟤 brown
        circle(m, 0x26AB, 49, 55, 61);      // ⚫ black
        m.put(0x26AA, new Glyph(Kind.CIRCLE, rgb(236, 237, 238), rgb(150, 153, 158), 0.82)); // ⚪ white
        // Coloured squares → rounded square.
        square(m, 0x1F7E5, 221, 46, 68);    // 🟥 red
        square(m, 0x1F7E7, 244, 144, 12);   // 🟧 orange
        square(m, 0x1F7E8, 253, 203, 88);   // 🟨 yellow
        square(m, 0x1F7E9, 120, 177, 89);   // 🟩 green
        square(m, 0x1F7E6, 85, 172, 238);   // 🟦 blue
        square(m, 0x1F7EA, 170, 142, 214);  // 🟪 purple
        square(m, 0x1F7EB, 193, 105, 79);   // 🟫 brown
        square(m, 0x2B1B, 49, 55, 61);      // ⬛ black
        m.put(0x2B1C, new Glyph(Kind.SQUARE, rgb(236, 237, 238), rgb(150, 153, 158), 0.80)); // ⬜ white
        // Triangle, diamonds, stars.
        m.put(0x1F53A, new Glyph(Kind.TRIANGLE, rgb(221, 46, 68), null, 0.95));  // 🔺 red triangle up
        m.put(0x1F536, new Glyph(Kind.DIAMOND, rgb(244, 144, 12), null, 1.0));   // 🔶 orange diamond
        m.put(0x1F537, new Glyph(Kind.DIAMOND, rgb(85, 172, 238), null, 1.0));   // 🔷 blue diamond
        m.put(0x1F538, new Glyph(Kind.DIAMOND, rgb(244, 144, 12), null, 0.7));   // 🔸 small orange diamond
        m.put(0x1F539, new Glyph(Kind.DIAMOND, rgb(85, 172, 238), null, 0.7));   // 🔹 small blue diamond
        m.put(0x2B50, new Glyph(Kind.STAR, rgb(255, 172, 51), null, 1.05));      // ⭐ star
        m.put(0x1F31F, new Glyph(Kind.STAR, rgb(255, 172, 51), null, 1.05));     // 🌟 glowing star
        return m;
    }

    private static void circle(Map<Integer, Glyph> m, int cp, int r, int g, int b) {
        m.put(cp, new Glyph(Kind.CIRCLE, rgb(r, g, b), null, 0.82));
    }

    private static void square(Map<Integer, Glyph> m, int cp, int r, int g, int b) {
        m.put(cp, new Glyph(Kind.SQUARE, rgb(r, g, b), null, 0.80));
    }

    private static DocumentColor rgb(int r, int g, int b) {
        return DocumentColor.rgb(r, g, b);
    }
}
