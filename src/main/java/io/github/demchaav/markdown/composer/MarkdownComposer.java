package io.github.demchaav.markdown.composer;

import com.demcha.compose.GraphCompose;
import com.demcha.compose.document.api.DocumentSession;
import com.demcha.compose.document.exceptions.DocumentRenderingException;
import com.demcha.compose.document.style.DocumentColor;
import io.github.demchaav.markdown.extension.CustomBlockParser;
import io.github.demchaav.markdown.mapper.FlexmarkAstMapper;
import io.github.demchaav.markdown.model.MarkdownDocument;
import io.github.demchaav.markdown.model.MarkdownNode;
import io.github.demchaav.markdown.parser.FlexmarkMarkdownParser;
import io.github.demchaav.markdown.render.InlineRenderer;
import io.github.demchaav.markdown.render.RenderContext;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.style.MarkdownStyles;
import io.github.demchaav.markdown.theme.tokens.PageTokens;

import java.io.OutputStream;
import java.nio.file.Path;
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
    private final MarkdownTheme theme;

    private MarkdownComposer(Builder builder) {
        this.parser = new FlexmarkMarkdownParser();
        this.mapper = new FlexmarkAstMapper();
        this.customBlockParser = new CustomBlockParser(parser, mapper);
        this.customBlocksEnabled = builder.customBlocks;
        this.theme = builder.theme;
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
     * @param theme the theme to render with
     * @return a new composer
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
        MarkdownDocument document = customBlocksEnabled
                ? customBlockParser.parse(markdown)
                : mapper.map(parser.parse(markdown));
        return new Rendered(document, theme);
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
        return new Rendered(mapper.map(flexmarkDocument), theme);
    }

    /**
     * Renders a pre-built semantic document — the stable hand-off point if you construct or
     * transform the {@link MarkdownDocument} model yourself.
     *
     * @param document the semantic document
     * @return a {@link Rendered} ready to write to PDF
     */
    public Rendered render(MarkdownDocument document) {
        return new Rendered(Objects.requireNonNull(document, "document"), theme);
    }

    /** Builder for {@link MarkdownComposer}. */
    public static final class Builder {

        private MarkdownTheme theme = DefaultMarkdownTheme.light();
        private boolean customBlocks = true;

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

        private Rendered(MarkdownDocument document, MarkdownTheme theme) {
            this.document = document;
            this.theme = theme;
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

        private DocumentSession newSession() {
            PageTokens page = theme.tokens().page();
            DocumentSession session = GraphCompose.document().pageSize(page.pageSize()).create();
            session.margin(page.margin());
            theme.fontFamilies().forEach(session::registerFontFamily);
            DocumentColor surface = theme.tokens().colors().surface();
            if (surface != null) {
                session.pageBackground(surface);
            }
            return session;
        }

        private void populate(DocumentSession session) {
            MarkdownStyles styles = new MarkdownStyles(theme.tokens());
            RenderContext ctx = new RenderContext(theme, styles, new InlineRenderer(), theme.imageResolver());
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
