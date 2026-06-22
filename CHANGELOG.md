# Changelog

All notable changes to **graph-compose-markdown** are documented here.
The format is loosely based on [Keep a Changelog](https://keepachangelog.com/),
and the project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- **Geometric emoji render as vector shapes.** Literal Unicode emoji that map cleanly to a
  shape — coloured circles (`🔴🟠🟡🟢🔵🟣🟤⚫⚪`), squares (`🟥🟧🟨🟩🟦🟪🟫⬛⬜`), `🔺`,
  diamonds (`🔶🔷🔸🔹`) and stars (`⭐🌟`) — now draw as native inline vector shapes in their
  conventional colour, instead of the missing-glyph `?` that PDF fonts produce (they have no
  colour-emoji glyphs). Uses the engine's inline-shape primitives, so it needs no font and no
  image. Other emoji are unchanged (the `:shortcode:` image path still applies); code spans stay
  verbatim.
- **Command-line renderer.** A standalone `cli/` module (outside the main build, like
  `examples/`) builds an executable fat-jar that renders Markdown to PDF from the shell:
  `java -jar graph-compose-markdown-cli.jar INPUT.md [-o OUT.pdf] [-t THEME] [-i IMAGES]
  [-e EMOJI] [--mono-jetbrains] [--strict]`, reads stdin via `-`, and picks any built-in
  theme pack. It is a thin picocli wrapper over `MarkdownComposer`; being out of the
  reactor, it never affects the published library artifact. Ships `gcmd` / `gcmd.cmd`
  launcher wrappers that resolve the jar relative to themselves (drop the `java -jar …`
  boilerplate). See the README "Command-line" section.
- **Themeable alert / callout accents — the `AlertColors` token group.** The per-variant
  accent colours for GitHub-style alerts (NOTE / TIP / IMPORTANT / WARNING / CAUTION) and for
  `:::` callouts (info / warning / error / success) were hard-coded inside the renderers, so a
  theme could not retune them — a dark theme got the same mid-saturation light-mode accents. They
  are now a `MarkdownTokens.alertColors()` group (with a `withAlertColors(...)` copy method), and
  the alert / callout renderers read from it. The default palette is the previous colours
  verbatim, so existing themes render identically; a theme can now supply accents tuned for its
  own surface.

### Changed
- **Alert icons.** GitHub-style alerts now render a vector icon next to the title, in the
  alert's accent colour — an info circle (Note), a lightbulb (Tip), a message bubble
  (Important), a warning triangle (Warning) and an octagon (Caution). The glyphs are drawn
  from small bundled SVGs (GitHub Octicons for Note / Important / Warning, MIT-licensed —
  see `render/icons/ATTRIBUTION.txt`) through the engine's inline-shape API (`RichText.shape`
  + `ShapeOutline.path`), filled so the `!` / `i` marks are crisp cut-outs at any size. They
  are true vector — no font glyph and no raster image — and
  are painted in the theme's accent, so they recolour automatically with the alert colours.
  If an icon resource is ever unavailable, the title still renders (just without a glyph).
- **The renderer dispatcher fails fast on an unregistered node type.** `RendererRegistry.render`
  previously skipped a node whose exact class had no registered renderer, silently dropping it; it
  now throws `IllegalStateException` naming the type. The built-in renderers cover every node type,
  so this only affects a deliberately partial theme — failing loudly upholds the no-silent-content-
  loss guarantee at the dispatch layer too.

### Fixed
- **`examples/` builds on a clean checkout again.** The detached `examples/` module still
  pinned `graph-compose-markdown:0.1.0-SNAPSHOT` after the `0.2.0` bump, so running an example
  on a fresh clone or CI runner failed with an unresolved-dependency error. Bumped it to
  `0.2.0-SNAPSHOT`, tracked through a single `gcmd.version` property (mirroring `cli/`) so the
  detached pom cannot drift behind the next snapshot.

### Build
- **CI now builds the detached `cli/` and `examples/` modules.** They sit outside the reactor,
  so the library's `verify` never compiled them — an API change that broke the CLI fat-jar or
  an example could ship CI-green. CI now installs the library and runs `cli` `package` +
  `examples` `test-compile` on the baseline JDK.

### Documentation
- Regenerated the committed showcase render (`assets/readme/showcase.*`) so the alert
  callouts show their new icons.
- Corrected several doc-vs-code inaccuracies: the `architecture.md` `MarkdownNode` permits list
  and inline enumeration (added `AlertNode` / `FrontMatterNode` / `UnsupportedBlockNode` and the
  emoji / unsupported inline runs); the parser-extension lists in the README and `architecture.md`
  and the `FlexmarkMarkdownParser` Javadoc (which named only GFM strikethrough — they now list
  emoji, autolink and YAML front matter too); the phantom `MarkdownStyles.HeadingStyle` reference
  in three docs (headings use an `InlineStyle` via `headingInline(level)`); the page background's
  home in `theming.md` (`ColorTokens.surface`, not `PageTokens`); the "bundled Twemoji PNGs"
  wording (the PNGs are user-supplied — the library bundles none); and the README status line and
  install-snippet version (`0.2.0`).

### Tests
- `AlertIconsTest` asserts every alert type ships a parseable vector icon (resource
  presence + parseability), so a missing or broken icon file fails the build.
- `EmojiShapesTest` asserts geometric emoji become inline shape runs (and plain text /
  non-geometric emoji are left untouched).
- Closed audit-flagged coverage gaps: `DefaultImageResolverTest` (null / blank input, the
  `http`/`https` no-fetch safety contract, and classpath + base-dir file resolution); nested
  strict-mode tests in `EngineRobustnessTest` proving the unsupported-content scan descends into
  list items, table cells, blockquotes and `:::` blocks (with a lenient-preservation mirror);
  `render((String) null)`, `customBlocks(false)` (`:::` stays literal text) and null-arg rejection
  for the Flexmark / semantic `render` overloads in `MarkdownComposerTest`; and
  `TableAlignmentRenderTest`, which reads PDF glyph positions to prove a right-aligned table cell
  renders further right than a left-aligned one.

## v0.1.0 — 2026-06-21

First public release — a themeable Markdown → GraphCompose document composer. Entries
are grouped by area.

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
- **YAML front matter.** A `---` … `---` metadata block at the top parses into a
  `FrontMatterNode` (accessible key → values) and renders a title block (`title` /
  `subtitle` / `author` / `date`) above a divider; other keys are kept for programmatic
  use. A `---` mid-document stays a thematic break.
- **Bare-URL autolinking.** Plain `https://…` URLs and email addresses in text become
  links (`flexmark-ext-autolink`). The mapper now also flattens Flexmark's `TextBase`
  wrapper node, so wrapped inline content is mapped rather than swallowed.
- **Links degrade instead of crashing.** A Markdown link with a relative / anchor /
  schemeless href (`[x](#section)`, `[doc](page.html)`, `[x](../up)`, `[x](/abs)`) now
  renders as link-styled text rather than aborting the whole render — the engine only
  annotates absolute-URI links, and a schemeless URI was throwing.
- **Emoji shortcodes.** `:rocket:` parses to an `EmojiRun`; a pluggable `EmojiResolver`
  (default: none) renders it as an **inline image** — e.g. `ClasspathEmojiResolver` pointed
  at `<shortcode>.png` files you supply on the classpath (such as Twemoji) — or it falls back
  to readable `:shortcode:` text. (PDF fonts have
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

[Unreleased]: https://github.com/DemchaAV/graphcompose-markdown/compare/v0.1.0...HEAD
[v0.1.0]: https://github.com/DemchaAV/graphcompose-markdown/releases/tag/v0.1.0
