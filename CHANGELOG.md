# Changelog

All notable changes to **graph-compose-markdown** are documented here.
The format is loosely based on [Keep a Changelog](https://keepachangelog.com/),
and the project follows [Semantic Versioning](https://semver.org/).

## v0.2.0 — Planned

### Public API
- **No silent content loss.** Unmodelled blocks (raw HTML, …) and inline HTML now
  surface as their original source — new `UnsupportedBlockNode` / `UnsupportedInlineRun`
  — rather than being dropped. `MarkdownComposer.builder().strictMode(true)` instead
  rejects such a document with `UnsupportedMarkdownException`.
- **The `:::` custom-block scanner respects code fences.** A `:::`-prefixed line inside
  a ``` ``` ```/`~~~` fenced code block is treated as code, not a custom-block fence.
- **Built themes are immutable.** A `MarkdownTheme`'s `RendererRegistry` is frozen when
  the theme is built; derive a new theme with `MarkdownTheme.builder(theme)` to change
  renderers (mutating a built theme's registry throws).
- **GFM tables.** `TableNode` / `TableCellNode` / `ColumnAlignment` in the model,
  parsed via `flexmark-ext-tables` and rendered by a `TableRenderer` (even
  fixed-width columns, styled header row, per-column alignment). Adds
  `MarkdownStyles.TableStyle` and a `SpacingTokens.tableCellPadding` token.
- **Theme packs.** Ready-made themes in `io.github.demchaav.markdown.theme.packs`:
  `GitHubTheme` (light/dark), `AcademicTheme`, `MinimalTheme`, `BusinessReportTheme`.
  Adds `DefaultMarkdownTheme.of(MarkdownTokens)` to assemble a theme from a token
  bundle with the standard renderers.
- **Task lists.** GFM `- [ ]` / `- [x]` items via `flexmark-ext-gfm-tasklist`;
  `ListItemNode.checked` carries the checkbox state and the list renderer draws a
  real inline checkbox (`RichText.checkbox` — a rounded frame with a centred
  check) in place of the bullet.
- **Footnotes.** GFM `[^id]` references (via `flexmark-ext-footnotes`) render as a
  small accent-coloured `[N]` on the baseline (the engine has no superscript), and
  definitions are collected into a document-end "Notes" section (`FootnotesNode`).
  Numbering is document-global, so references resolve correctly even when a `:::`
  custom block sits between a reference and its definition.
- **List bullets** are now vector shapes that vary by nesting depth (disc → ring →
  diamond) instead of a flat `•`.
- **Syntax highlighting.** Fenced code blocks are highlighted via a pluggable
  `SyntaxHighlighter` SPI; the built-in `RegexSyntaxHighlighter` covers ~15 common
  languages (keywords / strings / comments / numbers / annotations / call names) with
  no extra dependency. Adds the `SyntaxColors` token group (light + dark palettes) and
  `MarkdownTheme.builder().highlighter(...)` to plug a grammar-based highlighter.
  Indentation is preserved (non-breaking spaces); unknown/`text` languages stay plain.
- **Rich fonts (optional).** `BundledFonts.jetBrainsMonoCode(theme)` upgrades any
  theme to render code in **JetBrains Mono** instead of base-14 Courier, registering
  the family into the render session (`MarkdownTheme.builder().fontFamily(...)`). Adds
  the `FontFamily.MONO_JETBRAINS` family and `TypographyTokens.withCodeFamily(...)`.
  The `graph-compose-fonts` artifact is an **optional** dependency — the core stays
  base-14-only and dependency-free.
- **Render from a parsed tree.** `MarkdownComposer.render(...)` overloads accept an
  already-parsed Flexmark `Document` (bring your own parser/extensions) or a pre-built
  `MarkdownDocument` semantic model — no string round-trip or file.
- **Composable renderer packs.** `RendererPack` (with `StandardPack`) bundles node
  renderers so a project can ship and compose its own; `MarkdownTheme.builder` gains
  `.pack(...)`. `:::` custom blocks now dispatch by `type` —
  `.customBlock("chart", renderer)` / `RendererRegistry.registerCustomBlock(...)` —
  with the callout style as the fallback for unregistered types. Renderers remain
  keyed on the semantic model (parser-decoupled), not Flexmark nodes.
- **Rendering polish.** Code/quote/callout panels are kept whole across page
  breaks (`SectionBuilder.keepTogether()`); blockquotes gain a plate background;
  table body rows are filled (new `ColorTokens.tableRowBackground`) so tables read
  as a cohesive block in dark themes.

### Documentation
- README rewritten for public use: Flexmark → PDF framing, an advantages section, an
  architecture diagram, a theme-pack gallery and code samples with screenshots
  (`assets/readme/`). Added deep-dive guides — `docs/architecture.md`,
  `docs/theming.md`, `docs/custom-renderers.md` — plus `CONTRIBUTING.md`.
- **Runnable examples** in `examples/` (a standalone module): quickstart, render a
  Markdown file to a PDF, a theme gallery, and a custom `:::` block renderer.

### Tests
- **Fixture-driven smoke tests.** Markdown fixtures under
  `src/test/resources/markdown/` (`basic`, `lists`, `table`, `code`, `quote`,
  `mixed-inline`, and a `master` page that exercises the whole parser). Each is
  asserted to parse, render to a non-empty multi-page PDF, and carry its expected
  text into the output (extracted with PDFBox `PDFTextStripper`). The master page is
  also rendered through every theme pack and is now the single source for the preview
  PDFs.
- Table mapper test (alignments / header / rows) and a table render smoke test;
  a theme-packs test renders the same content through every pack.
- **Engine hardening tests** (`EngineRobustnessTest`): `:::` inside a fenced code block
  stays code, unsupported HTML (block + inline) is surfaced not dropped, strict mode
  rejects it, and a built theme's registry is immutable.

## v0.1.0 — Planned

First public preview: a themeable Markdown → GraphCompose document composer.

### Public API
- `MarkdownComposer` — parse Markdown and render it to PDF
  (`toPdfBytes()`, `writePdf(Path)`, `writePdf(OutputStream)`).
- `MarkdownTheme` three-layer model — design tokens (`MarkdownTokens`),
  component styles, and node renderers (`NodeRenderer`) bound in a
  `RendererRegistry`; `MarkdownTheme.builder(base)` composes a new theme from
  an existing one by overriding tokens, styles, or individual renderers.
- `DefaultMarkdownTheme` — light and dark variants built on the PDF base-14
  fonts (no extra font artifact required).
- Supported elements: headings (h1–h6), paragraphs with inline runs
  (bold / italic / strikethrough / inline code / links), ordered & unordered
  (nested) lists, fenced code blocks, blockquotes, horizontal rules, images.
- Extension seams: `:::` custom blocks (`CustomBlockNode` + registered
  `NodeRenderer`, with an example callout), and an `ImageResolver` SPI.

### Documentation
- README quickstart and theming guide.

### Tests
- Mapper unit tests (Flexmark AST → semantic tree).
- Render smoke tests (output begins with `%PDF-`).
- Theme-composition test (reuse a base theme, override one renderer/token).

[Unreleased]: https://github.com/DemchaAV/graphcompose-markdown/commits/main
