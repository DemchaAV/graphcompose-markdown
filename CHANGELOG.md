# Changelog

All notable changes to **graph-compose-markdown** are documented here.
The format is loosely based on [Keep a Changelog](https://keepachangelog.com/),
and the project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- **PDFs open with the bookmark panel visible.** A rendered document that has at least one
  heading now writes the viewer preference `PageMode.USE_OUTLINES`, so PDF readers open the
  bookmark/outline sidebar and surface the heading tree the library already emits. Heading-less
  documents are untouched (no empty panel), and `MarkdownComposer.builder().openOutline(false)`
  turns the preference off. PDF-only; other backends ignore it.
- **Rasterize straight to images — `Rendered.toImages(dpi)` / `toImage(pageIndex, dpi)`.**
  Renders the document directly to one `BufferedImage` per page (or a single page) without the
  previous PDF-write → re-parse round-trip — for thumbnails, web preview cards, and CI visual
  snapshots. Thin wrappers over the engine's 1.9 `DocumentSession.toImages(...)`. Runnable
  `PngExportExample`.
- **Running footer with page numbers — `FooterTokens`.** A new token group adds an optional page
  footer whose `left`/`center`/`right` text may use the `{page}` / `{pages}` / `{date}` placeholders
  the PDF backend substitutes per page. It is **disabled by default** (single-page/screen output is
  unchanged); enable it with `tokens().withFooter(FooterTokens.pageNumbers())` for a centred
  "Page N of M", or build a custom `FooterTokens` (text, font size, colour, `showOnFirstPage`).
  Wired through the engine's `DocumentSession.footer(...)` / `DocumentPageNumbering`. Runnable
  `FooterExample`.

## v0.2.0 — 2026-07-01

### Added
- **Auto-generated clickable table of contents — a `[TOC]` marker.** A standalone `[TOC]`
  (or `[[_TOC_]]`, any case) line expands into a generated, clickable list of links to every
  heading, nested by heading level and indented accordingly. Each entry is a native in-document
  jump to the heading's anchor, so the TOC works from a PDF viewer. Heading slugs are planned up
  front, so a `[TOC]` placed *above* its headings still resolves (forward references), and
  duplicate-text headings get the same de-duplicated slugs (`-1`, `-2`) in both the TOC and the
  headings. New `TocNode` model type + `BuiltinRenderers.TocRenderer`; empty-text headings are
  skipped and a document with no headings renders nothing. Built entirely on the existing heading
  anchors + internal-link primitives.
- **`MarkdownComposer.renderFile(Path)` — a file-in entry point.** The library's `render(...)`
  methods take a Markdown *string* (or a parsed AST); reading the file was left to the caller.
  `renderFile(Path)` now reads a Markdown file (UTF-8) and renders it, **resolving relative image
  paths against the file's own directory** — so a standalone `doc.md` with `![](diagram.png)` next
  to it renders without wiring an `ImageResolver` by hand. It returns the usual `Rendered`
  (`writePdf` / `toPdfBytes`). The composer and its theme are unchanged (the directory is used as the
  image base for that one render). A `renderFile(Path, ImageResolver)` overload takes an explicit
  resolver (classpath, CDN, …) instead of the file's directory.
- **In-document navigation (clickable `[text](#heading)` links and footnotes).** Built on the
  GraphCompose 1.9 anchor/internal-link API. Every heading now declares a GitHub-style anchor
  slug (lower-cased, punctuation stripped, spaces hyphenated, with `-1`/`-2` suffixes for
  duplicate-text headings), so a `[jump](#my-heading)` link becomes a native PDF `GoTo` action
  instead of inert styled text — previously such relative/anchor hrefs could not be annotated and
  rendered as plain text. Footnotes are bidirectional: a `[N]` reference jumps to its note
  (`fn-N`), and the note's marker jumps back to the citation (`fnref-N`), the back-anchor placed
  on the first block that cites each footnote. Fragment matching slugifies the link target the
  same way headings do, so both `#My Heading` and `#my-heading` resolve. A `#fragment` always
  becomes an internal link; if no heading declares that anchor the engine renders it as plain text
  (its unknown-anchor fallback) rather than crashing. External `http(s)` links are unchanged.
- **Geometric emoji render as vector shapes.** Literal Unicode emoji that map cleanly to a
  shape — coloured circles (`🔴🟠🟡🟢🔵🟣🟤⚫⚪`), squares (`🟥🟧🟨🟩🟦🟪🟫⬛⬜`), `🔺`,
  diamonds (`🔶🔷🔸🔹`) and stars (`⭐🌟`) — now draw as native inline vector shapes in their
  conventional colour, instead of the missing-glyph `?` that PDF fonts produce (they have no
  colour-emoji glyphs). Uses the engine's inline-shape primitives, so it needs no font and no
  image. They render in flowing text, **inline code, and fenced code blocks** — in code a literal
  `🔴`/`🟡`/`🟢` (e.g. a colour-coded legend) would otherwise hit the PDF mono font, which has no
  emoji glyph, and come out as `?`; drawing the shape is strictly more faithful, and all other
  code text stays verbatim (an emoji-bearing inline-code span forgoes its chip, since the chip
  background can't host a shape). Other (non-geometric) emoji are unchanged (the `:shortcode:`
  image path still applies).
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
- **Per-token `withX` copy methods on the leaf token records.** `ColorTokens` and
  `TypographyTokens` previously exposed only `withCodeBackground`/`withAccent` and `withCodeFamily`,
  so overriding any other single token meant rebuilding the whole record by hand. Added the full
  set — every `ColorTokens` colour (`withText`, `withMuted`, `withHeading`, `withLink`, `withCode`,
  `withTableRowBackground`, `withQuoteBar`, `withQuoteText`, `withRule`, `withSurface` — the last
  accepts `null` for no page fill) and every `TypographyTokens` field — so the documented "override
  one token without copy-paste" workflow holds.
- **Link underlining is now a token — `ShapeTokens.underlineLinks` (default `true`).** It was
  hardcoded, so a theme could not choose coloured-but-not-underlined links without replacing the
  style layer (e.g. `MinimalTheme`, which colours links the same as body text, relied on the
  hardcoded underline). `MarkdownStyles` now reads it from the theme's `ShapeTokens`; the two-arg
  `ShapeTokens(cornerRadius, ruleThickness)` constructor still defaults it to `true`, so existing
  themes are unchanged.

### Changed
- **GraphCompose engine bumped 1.8.0 → 1.9.0.** Picks up the in-document navigation API
  (anchors, internal `linkTo`, the `linkTarget()` accessor) and inline highlight chips used by
  the features above. No source changes required of consumers; the new `linkTarget()` on inline
  runs supersedes the now-deprecated `linkOptions()` bridge, which still works for external links.
- **Inline `code` renders on a rounded chip.** Inline code spans now sit on a padded, rounded
  background (the GitHub inline-`code` look) via the engine's inline highlight-chip primitive,
  instead of bare monospace text. The chip fill is theme-aware — it reads the theme's
  `codeBackground` token, so dark themes get a dark chip — added as a new `InlineStyle.codeBackground`
  component. Code text stays verbatim; code inside a link keeps the link styling (no chip).
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
- **Loose lists now render with wider spacing than tight lists.** CommonMark distinguishes a loose
  list (items separated by blank lines) from a tight one, but both used to render identically. The
  mapper now carries a `loose` flag on `ListNode` (read from Flexmark's `ListBlock.isLoose()`), and
  the list renderer spaces a loose list's items with the block (paragraph) spacing instead of the
  compact item spacing. `ListNode` keeps a three-arg constructor (tight) for source compatibility.

### Fixed
- **Body line spacing now honours the `lineSpacing` multiplier (text was cramped).** The
  renderers fed the typography `lineSpacing` token — documented as a multiplier (1.0 = single)
  — straight into the engine's `ParagraphBuilder.lineSpacing(...)`, which takes **points**. So
  a 1.4 "multiplier" added only ~1.4 pt of leading and wrapped lines in paragraphs, list items
  and footnotes read as dense. The value is now converted to extra leading in points
  (`(multiplier − 1) × fontSize`) via the new `MarkdownStyles.lineLeading(...)`, so at 11 pt body
  1.4 adds ~4.4 pt. Every theme's existing multiplier (1.35–1.5) now produces the airy spacing it
  implied; the default theme is nudged 1.35 → 1.4. Code blocks keep their tight leading.
- **`examples/` builds on a clean checkout again.** The detached `examples/` module still
  pinned `graph-compose-markdown:0.1.0-SNAPSHOT` after the `0.2.0` bump, so running an example
  on a fresh clone or CI runner failed with an unresolved-dependency error. Bumped it to
  `0.2.0-SNAPSHOT`, tracked through a single `gcmd.version` property (mirroring `cli/`) so the
  detached pom cannot drift behind the next snapshot.
- **Defined reference links and images now render as links/images.** A `[text][ref]` link or
  `![alt][ref]` image with a matching `[ref]: url` definition was emitted as literal bracketed
  text, and the `[ref]: url` definition line itself leaked into the PDF as muted monospace.
  Flexmark keeps both as `LinkRef`/`ImageRef` nodes (only *inline* links become `Link` nodes), so
  the mapper now resolves the definition to a real `LinkRun` / `ImageRun` — falling back to literal
  source only when the reference is genuinely undefined — and drops the definition line.
- **The CLI no longer dumps a stack trace on errors.** Failures outside the narrow render block —
  an unwritable output directory, a missing bundled font, or a non-UTF-8 input file — leaked a raw
  Java stack trace. The CLI now installs a picocli execution-exception handler that prints a clean
  `error: <message>` with a non-zero exit, and a non-UTF-8 input file (a Windows ANSI / UTF-16 /
  BOM file) is reported as `error: cannot read … as UTF-8` (exit 2) instead of a
  `MalformedInputException` trace.
- **CLI: `-o -` writes the PDF to stdout** (the status line stays on stderr), so the renderer can
  be piped — `cat notes.md | gcmd - -o - > notes.pdf`. Previously `-o -` created a file literally
  named `-`. Also corrected the `--mono-jetbrains` help text, which wrongly implied an extra
  dependency is needed (the font is bundled in the CLI fat-jar).
- **Inline images now render instead of showing only their alt text.** An image sitting amid other
  inline content (`![alt](src)` — a badge or logo in a sentence) was dropped to its alt text
  because the inline renderer never received the theme's `ImageResolver`. It now resolves the
  source and draws the image inline at line height with its aspect ratio preserved (and carries any
  surrounding link), falling back to alt text only when the source cannot be resolved.
- **Geometric emoji inside link text render as shapes again.** A literal geometric emoji typed
  inside a link (e.g. `[🔴 Critical](url)`) hit the inline renderer's link branch, which returned
  before the geometric-emoji shape mapping — so it came out as a missing glyph. The link branch now
  maps the emoji to its inline shape (with the surrounding words kept as a clickable link).

### Build
- **CI now builds the detached `cli/` and `examples/` modules.** They sit outside the reactor,
  so the library's `verify` never compiled them — an API change that broke the CLI fat-jar or
  an example could ship CI-green. CI now installs the library and runs `cli` `package` +
  `examples` `test-compile` on the baseline JDK.

### Security
- **Opt-in image-resolver sandbox.** `DefaultImageResolver` resolves a file source as-is by
  default, so for untrusted Markdown an absolute path (`![x](/etc/passwd)`) or a `../` escape could
  read an arbitrary local file into the PDF. New `DefaultImageResolver.sandboxed(baseDir)` (and a
  `new DefaultImageResolver(baseDir, true)` constructor) confine filesystem resolution to `baseDir`,
  rejecting absolute paths and `../` traversals; classpath resources are unaffected. The default
  behaviour is unchanged — sandboxing is the explicit choice for untrusted input.

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
- Corrected the `LineBreakRun` Javadoc (a hard break is two trailing spaces or `<br>` — a trailing
  backslash is not recognised by the parser and degrades to a soft break), and noted on `ImageNode`
  / `ImageRun` / `LinkRun` that the `title` is captured for programmatic use but not currently
  rendered. Added bug-report and feature-request issue templates under `.github/ISSUE_TEMPLATE/`.
- `MarkdownComposer.create` now documents its `@throws NullPointerException` for a null theme
  (matching the Builder), and `custom-renderers.md` states plainly that a `:::` block
  (`CustomBlockNode`) is the only seam for a third-party block type — the sealed model does not
  allow new `MarkdownNode` subtypes.

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
- `MarkdownCliTest` drives the CLI in-process through its real wiring — the `0`/`2` exit-code
  contract (happy render, missing input, unknown theme, `--version`, `-o -` stdout) plus the
  non-UTF-8 and unwritable-output error paths, asserting each yields a clean `error:` message with
  no leaked stack frame. The `cli/` module had no tests before; it now pins its own JUnit / AssertJ
  / Surefire so `-f cli/pom.xml package` runs them.
- `TableAndListEdgeCasesTest` renders a ragged GFM table (a body row with fewer cells than the
  header), a table with empty cells, and a 3-level nested list, asserting each renders and keeps
  every cell / item's text (the fixture suite previously had only well-formed tables and 2-level
  lists).

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
