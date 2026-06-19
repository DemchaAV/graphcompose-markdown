# Changelog

All notable changes to **graph-compose-markdown** are documented here.
The format is loosely based on [Keep a Changelog](https://keepachangelog.com/),
and the project follows [Semantic Versioning](https://semver.org/).

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
