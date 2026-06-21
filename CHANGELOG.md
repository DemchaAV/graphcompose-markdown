# Changelog

All notable changes to **graph-compose-markdown** are documented here.
The format is loosely based on [Keep a Changelog](https://keepachangelog.com/),
and the project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

Nothing has been published yet. Everything below ships in the **first release**
(`0.1.0`) — there is no earlier released version, so entries are grouped by area, not
by version.

### Public API — foundation
- `MarkdownComposer` — parse Markdown and render to PDF (`toPdfBytes()`,
  `writePdf(Path)`, `writePdf(OutputStream)`), plus overloads that accept an
  already-parsed Flexmark `Document` or a pre-built `MarkdownDocument` model.
- `MarkdownTheme` three-layer model — design tokens (`MarkdownTokens`), component
  styles (`MarkdownStyles`), and node renderers (`NodeRenderer`) bound in a
  `RendererRegistry`. `MarkdownTheme.builder(base)` derives a new theme by overriding
  tokens, styles, or individual renderers; a built theme is immutable.
- `DefaultMarkdownTheme` — light and dark variants on the PDF base-14 fonts (no font
  artifact required); `DefaultMarkdownTheme.of(MarkdownTokens)` assembles a theme from
  a token bundle with the standard renderers.
- Elements: headings (h1–h6), paragraphs with inline **bold** / *italic* /
  ~~strikethrough~~ / `inline code` / links, ordered & unordered (nested) lists,
  **task lists**, fenced code blocks, blockquotes, horizontal rules, images, **GFM
  tables** (per-column alignment), and **footnotes** (document-global numbering, a
  document-end "Notes" section).
- Extension seams: `:::` custom blocks (`CustomBlockNode` + a registered `NodeRenderer`,
  dispatched by `type`), `RendererPack` / `StandardPack`, `ImageResolver`, and a
  `SyntaxHighlighter` SPI.

### Public API — notable behaviour
- **Emoji shortcodes.** `:rocket:` parses to an `EmojiRun`; a pluggable `EmojiResolver`
  (default: none) renders it as an **inline image** — e.g. `ClasspathEmojiResolver` with
  bundled Twemoji PNGs — or it falls back to readable `:shortcode:` text. (PDF fonts have
  no emoji glyphs, so text is the honest default; the SPI lets an inline-SVG or font
  strategy drop in later without other changes.)
- **GitHub-style alerts.** A blockquote whose first line is `[!NOTE]` / `[!TIP]` /
  `[!IMPORTANT]` / `[!WARNING]` / `[!CAUTION]` renders as a titled, colour-coded callout
  (new `AlertNode` / `AlertType`); any other blockquote stays a plain quote. The marker
  must be alone on the first line (GitHub's rule).
- **PDF outline from headings.** Headings (h1–h6) become a nested PDF bookmark tree, so
  the viewer's outline/navigation pane mirrors the document structure (titles are the
  plain heading text; nesting follows heading level).
- **Composable renderer packs.** `.pack(...)` bundles renderers; `:::` blocks dispatch
  by `type` (`.customBlock("chart", renderer)`), with the callout style as the fallback.
  Renderers stay keyed on the semantic model (parser-decoupled), not Flexmark nodes.
- **Theme packs.** `GitHubTheme` (light/dark), `AcademicTheme`, `MinimalTheme`,
  `BusinessReportTheme` in `io.github.demchaav.markdown.theme.packs`.
- **Syntax highlighting.** A pluggable SPI; the built-in `RegexSyntaxHighlighter` covers
  ~15 languages with no extra dependency. Colours come from the `SyntaxColors` token
  group (light/dark); indentation is preserved; unknown/`text` languages stay plain.
- **Rich fonts (optional).** `BundledFonts.jetBrainsMonoCode(theme)` renders code in
  JetBrains Mono (`FontFamily.MONO_JETBRAINS` + `MarkdownTheme.builder().fontFamily(...)`);
  `graph-compose-fonts` is an **optional** dependency so the core stays base-14-only.
- **No silent content loss.** Unmodelled blocks (raw HTML, …) and inline HTML surface as
  their source via `UnsupportedBlockNode` / `UnsupportedInlineRun`;
  `MarkdownComposer.builder().strictMode(true)` instead throws `UnsupportedMarkdownException`.
- **Robust `:::` parsing.** The custom-block scanner respects ``` ``` ```/`~~~` code
  fences, so a `:::` line inside a code block stays code.
- **Rendering polish.** Vector list bullets that vary by nesting depth; real checkbox
  task items; code/quote/callout panels kept whole across page breaks
  (`SectionBuilder.keepTogether()`); filled table body rows (`ColorTokens.tableRowBackground`).

### Documentation
- Public-facing README (Flexmark → PDF framing, an advantages section, an architecture
  diagram, a theme-pack gallery and code samples with screenshots under
  `assets/readme/`); deep-dive guides `docs/architecture.md`, `docs/theming.md`,
  `docs/custom-renderers.md`; `CONTRIBUTING.md`; and runnable `examples/`.

### Tests
- Mapper unit tests (Flexmark AST → semantic tree); fixture-driven render tests over
  `src/test/resources/markdown/` that extract text with PDFBox `PDFTextStripper` and
  assert it survives; a master page rendered through every theme pack;
  theme-composition tests; and `EngineRobustnessTest` (code-fence `:::`,
  unsupported-content handling, strict mode, theme immutability).

[Unreleased]: https://github.com/DemchaAV/graphcompose-markdown/commits/main
