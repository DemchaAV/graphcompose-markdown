package io.github.demchaav.markdown.composer;

import com.demcha.compose.GraphCompose;
import com.demcha.compose.document.api.DocumentSession;
import com.demcha.compose.document.exceptions.DocumentRenderingException;
import com.demcha.compose.document.output.DocumentHeaderFooter;
import com.demcha.compose.document.output.DocumentHeaderFooterZone;
import com.demcha.compose.document.output.DocumentPageNumbering;
import com.demcha.compose.document.output.DocumentViewerPreferences;
import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.extension.CustomBlockParser;
import io.github.demchaav.markdown.extension.DefaultImageResolver;
import io.github.demchaav.markdown.extension.ImageResolver;
import io.github.demchaav.markdown.mapper.FlexmarkAstMapper;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.parser.FlexmarkMarkdownParser;
import io.github.demchaav.markdown.render.InlineRenderer;
import io.github.demchaav.markdown.render.RenderContext;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import io.github.demchaav.markdown.theme.tokens.FooterTokens;
import io.github.demchaav.markdown.theme.tokens.PageTokens;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * The library's entry point: parse Markdown, apply a {@link MarkdownTheme}, and
 * render a GraphCompose document.
 *
 * <pre>{@code
 * MarkdownComposer composer = MarkdownComposer.builder()
 *         .theme(DefaultMarkdownTheme.light())
 *         .build();
 * composer.render("# Hello\n\nWorld").writePdf(Path.of("out.pdf"));
 * }</pre>
 *
 * <p>A composer is immutable and thread-safe; the parser and mapper it holds are
 * reusable across calls.</p>
 */
public final class MarkdownComposer {

    private final FlexmarkMarkdownParser parser;
    private final FlexmarkAstMapper mapper;
    private final CustomBlockParser customBlockParser;
    private final boolean customBlocksEnabled;
    private final boolean strict;
    private final boolean openOutline;
    private final MarkdownTheme theme;

    private MarkdownComposer(Builder builder) {
        this.parser = new FlexmarkMarkdownParser();
        this.mapper = new FlexmarkAstMapper();
        this.customBlockParser = new CustomBlockParser(parser, mapper);
        this.customBlocksEnabled = builder.customBlocks;
        this.strict = builder.strict;
        this.openOutline = builder.openOutline;
        this.theme = builder.theme;
    }

    /**
     * Builds a {@link Rendered}, first rejecting unsupported content if strict mode is on.
     * In the default lenient mode unsupported blocks/inline are surfaced as raw text instead.
     */
    private Rendered toRendered(MarkdownDocument document) {
        return toRendered(document, theme);
    }

    private Rendered toRendered(MarkdownDocument document, MarkdownTheme renderTheme) {
        if (strict) {
            String unsupported = UnsupportedScanner.firstUnsupported(document);
            if (unsupported != null) {
                throw new UnsupportedMarkdownException("strict mode: " + unsupported);
            }
        }
        return new Rendered(document, renderTheme, openOutline);
    }

    /** Parses Markdown into the semantic model, honouring the {@code customBlocks} setting. */
    private MarkdownDocument parse(String markdown) {
        return customBlocksEnabled
                ? customBlockParser.parse(markdown)
                : mapper.map(parser.parse(markdown));
    }

    /**
     * Starts a composer builder (defaults to {@link DefaultMarkdownTheme#light()}).
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a composer for a theme.
     *
     * @param theme the theme to render with; must not be {@code null}
     * @return a new composer
     * @throws NullPointerException if {@code theme} is {@code null}
     */
    public static MarkdownComposer create(MarkdownTheme theme) {
        return builder().theme(theme).build();
    }

    /** @return the theme this composer renders with */
    public MarkdownTheme theme() {
        return theme;
    }

    /**
     * Parses Markdown into a renderable document.
     *
     * @param markdown the Markdown source ({@code null} is treated as empty)
     * @return a {@link Rendered} ready to write to PDF
     */
    public Rendered render(String markdown) {
        return toRendered(parse(markdown));
    }

    /**
     * Reads a Markdown <em>file</em> (UTF-8) and renders it, resolving relative image paths in the
     * document against the file's own directory — so a {@code ![](diagram.png)} sitting next to the
     * {@code .md} just works without wiring an {@code ImageResolver} by hand.
     *
     * <pre>{@code
     * MarkdownComposer.create(theme).renderFile(Path.of("doc.md")).writePdf(Path.of("doc.pdf"));
     * }</pre>
     *
     * <p>The file's directory is used as the image base for this render only; the composer and its
     * theme are unchanged. Every other theme setting (emoji resolver, renderers, tokens, fonts) is
     * kept as-is. To supply a custom {@code ImageResolver} instead of the file's directory, use
     * {@link #renderFile(Path, ImageResolver)}.</p>
     *
     * @param file the Markdown file to read and render; must not be {@code null}
     * @return a {@link Rendered} ready to write to PDF
     * @throws IOException          if the file cannot be read
     * @throws NullPointerException if {@code file} is {@code null}
     */
    public Rendered renderFile(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        Path dir = file.toAbsolutePath().getParent();
        ImageResolver resolver = dir == null ? theme.imageResolver() : new DefaultImageResolver(dir);
        return renderFile(file, resolver);
    }

    /**
     * Reads a Markdown file (UTF-8) and renders it, resolving relative images through the given
     * {@code imageResolver} — the explicit form of {@link #renderFile(Path)} for callers who want
     * a custom resolver (classpath, CDN, …) instead of the file's own directory.
     *
     * <p>The resolver applies to this render only; the composer and its theme are unchanged. Every
     * other theme setting (emoji resolver, renderers, tokens, fonts) is kept as-is.</p>
     *
     * @param file          the Markdown file to read and render; must not be {@code null}
     * @param imageResolver the resolver for relative image sources; must not be {@code null}
     * @return a {@link Rendered} ready to write to PDF
     * @throws IOException          if the file cannot be read
     * @throws NullPointerException if {@code file} or {@code imageResolver} is {@code null}
     */
    public Rendered renderFile(Path file, ImageResolver imageResolver) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(imageResolver, "imageResolver");
        String markdown = Files.readString(file, StandardCharsets.UTF_8);
        MarkdownTheme themeForFile = MarkdownTheme.builder(theme).imageResolver(imageResolver).build();
        return toRendered(parse(markdown), themeForFile);
    }

    /**
     * Renders an already-parsed Flexmark document — for callers who already hold a
     * {@code com.vladsch.flexmark.util.ast.Document} (e.g. parsed with their own Flexmark
     * parser and extensions) and want a PDF without a string round-trip.
     *
     * <p>The tree is mapped and rendered as-is. {@code :::} custom blocks are a text-level
     * pre-pass, so they are <em>not</em> extracted here — use {@link #render(String)} if you
     * rely on them.</p>
     *
     * @param flexmarkDocument the Flexmark AST root
     * @return a {@link Rendered} ready to write to PDF
     */
    public Rendered render(com.vladsch.flexmark.util.ast.Document flexmarkDocument) {
        Objects.requireNonNull(flexmarkDocument, "flexmarkDocument");
        return toRendered(mapper.map(flexmarkDocument));
    }

    /**
     * Renders a pre-built semantic document — the stable hand-off point if you construct or
     * transform the {@link MarkdownDocument} model yourself.
     *
     * @param document the semantic document
     * @return a {@link Rendered} ready to write to PDF
     */
    public Rendered render(MarkdownDocument document) {
        return toRendered(Objects.requireNonNull(document, "document"));
    }

    /** Builder for {@link MarkdownComposer}. */
    public static final class Builder {

        private MarkdownTheme theme = DefaultMarkdownTheme.light();
        private boolean customBlocks = true;
        private boolean strict = false;
        private boolean openOutline = true;

        private Builder() {
        }

        /**
         * Sets the theme.
         *
         * @param newTheme the theme
         * @return this builder
         */
        public Builder theme(MarkdownTheme newTheme) {
            this.theme = Objects.requireNonNull(newTheme, "theme");
            return this;
        }

        /**
         * Enables or disables {@code :::} custom-block parsing (default enabled).
         *
         * @param enabled whether to extract {@code :::} fenced blocks
         * @return this builder
         */
        public Builder customBlocks(boolean enabled) {
            this.customBlocks = enabled;
            return this;
        }

        /**
         * Enables strict mode (default off). In strict mode {@link #render} throws an
         * {@link UnsupportedMarkdownException} if the document contains content the
         * library cannot render faithfully (a raw HTML block, unmodelled inline HTML,
         * …). In the default lenient mode that content is surfaced as raw text instead
         * of being silently dropped.
         *
         * @param enabled whether to reject unsupported content
         * @return this builder
         */
        public Builder strictMode(boolean enabled) {
            this.strict = enabled;
            return this;
        }

        /**
         * Controls whether a rendered PDF asks the viewer to open its bookmark/outline panel
         * (default on). The preference is only written when the document actually has at least
         * one bookmarkable heading, so heading-less documents are unaffected either way.
         *
         * @param enabled whether to open the outline panel in the PDF viewer
         * @return this builder
         */
        public Builder openOutline(boolean enabled) {
            this.openOutline = enabled;
            return this;
        }

        /**
         * Builds the composer.
         *
         * @return a new {@link MarkdownComposer}
         */
        public MarkdownComposer build() {
            return new MarkdownComposer(this);
        }
    }

    /**
     * A parsed, themed document ready to be written to PDF. May be rendered to
     * more than one sink; each call builds a fresh session.
     */
    public static final class Rendered {

        private final MarkdownDocument document;
        private final MarkdownTheme theme;
        private final boolean openOutline;

        private Rendered(MarkdownDocument document, MarkdownTheme theme, boolean openOutline) {
            this.document = document;
            this.theme = theme;
            this.openOutline = openOutline;
        }

        /** @return the underlying semantic document tree */
        public MarkdownDocument document() {
            return document;
        }

        /**
         * Renders to a PDF byte array.
         *
         * @return the PDF bytes
         * @throws DocumentRenderingException if rendering fails
         */
        public byte[] toPdfBytes() throws DocumentRenderingException {
            try (DocumentSession session = newSession()) {
                populate(session);
                return session.toPdfBytes();
            }
        }

        /**
         * Renders to a PDF file.
         *
         * @param path the output file
         * @throws DocumentRenderingException if rendering fails
         */
        public void writePdf(Path path) throws DocumentRenderingException {
            try (DocumentSession session = newSession()) {
                populate(session);
                session.buildPdf(path);
            }
        }

        /**
         * Renders to a PDF output stream.
         *
         * @param output the output stream
         * @throws DocumentRenderingException if rendering fails
         */
        public void writePdf(OutputStream output) throws DocumentRenderingException {
            try (DocumentSession session = newSession()) {
                populate(session);
                session.writePdf(output);
            }
        }

        /**
         * Rasterizes the document straight to one image per page — no PDF write / re-parse
         * round-trip — for thumbnails, web previews, or visual snapshots.
         *
         * @param dpi the render resolution in dots per inch (e.g. 96 for screen, 150+ for print)
         * @return one {@link BufferedImage} per page, in page order
         * @throws IllegalArgumentException   if {@code dpi <= 0}
         * @throws DocumentRenderingException if rendering fails
         */
        public List<BufferedImage> toImages(int dpi) throws DocumentRenderingException {
            try (DocumentSession session = newSession()) {
                populate(session);
                return session.toImages(dpi);
            }
        }

        /**
         * Rasterizes a single page to an image.
         *
         * @param pageIndex the 0-based page index
         * @param dpi       the render resolution in dots per inch
         * @return the page as a {@link BufferedImage}
         * @throws IllegalArgumentException   if {@code dpi <= 0}
         * @throws IndexOutOfBoundsException  if {@code pageIndex} is out of range
         * @throws DocumentRenderingException if rendering fails
         */
        public BufferedImage toImage(int pageIndex, int dpi) throws DocumentRenderingException {
            try (DocumentSession session = newSession()) {
                populate(session);
                return session.toImage(pageIndex, dpi);
            }
        }

        private DocumentSession newSession() {
            PageTokens page = theme.tokens().page();
            DocumentSession session = GraphCompose.document().pageSize(page.pageSize()).create();
            session.margin(page.margin());
            theme.fontFamilies().forEach(session::registerFontFamily);
            DocumentColor surface = theme.tokens().colors().surface();
            if (surface != null) {
                session.pageBackground(surface);
            }
            FooterTokens footer = theme.tokens().footer();
            if (footer.enabled()) {
                session.footer(DocumentHeaderFooter.builder()
                        .zone(DocumentHeaderFooterZone.FOOTER)
                        .leftText(footer.leftText())
                        .centerText(footer.centerText())
                        .rightText(footer.rightText())
                        .fontSize((float) footer.fontSize())
                        .textColor(footer.color())
                        .numbering(DocumentPageNumbering.builder()
                                .showOnFirstPage(footer.showOnFirstPage())
                                .build())
                        .build());
            }
            return session;
        }

        private void populate(DocumentSession session) {
            MarkdownStyles styles = new MarkdownStyles(theme.tokens());
            RenderContext ctx = new RenderContext(theme, styles,
                    new InlineRenderer(theme.emojiResolver(), theme.imageResolver()), theme.imageResolver());
            // Plan heading slugs (and the TOC entry list) up front so a [TOC] above its headings
            // links to the same anchors the headings declare.
            ctx.planHeadings(document);
            // Ask the viewer to open its bookmark panel — but only when there is an outline to show.
            if (openOutline && ctx.hasBookmarkableHeading()) {
                session.viewerPreferences(DocumentViewerPreferences.openOutline());
            }
            session.pageFlow(flow -> flow.addSection("Body", body -> {
                body.spacing(styles.blockSpacing());
                if (document.blocks().isEmpty()) {
                    body.addParagraph(p -> p.text(" "));
                    return;
                }
                for (MarkdownNode block : document.blocks()) {
                    ctx.renderBlock(block, body);
                }
            }));
        }
    }
}
