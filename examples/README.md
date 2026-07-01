# Examples

Runnable examples for **graphcompose-markdown**. This is a standalone Maven module
(not part of the main build), so build the library first, then run any example.

## 1. Build the library

From the repository root:

```bash
./mvnw -B -ntp -DskipTests install
```

This installs `io.github.demchaav:graph-compose-markdown:0.1.0-SNAPSHOT` into your
local Maven repository, which these examples depend on.

## 2. Run an example

From this `examples/` directory (use `../mvnw.cmd` on Windows):

```bash
../mvnw -f pom.xml exec:java -Dexec.mainClass=<class>
```

| Example | What it does | Output |
|---------|--------------|--------|
| `…examples.QuickStartExample` | An inline Markdown string → PDF | `quickstart.pdf` |
| `…examples.RenderMarkdownFileExample` | Reads a **Markdown file** → writes a **PDF file** | `sample.pdf`, or your `-Dexec.args` |
| `…examples.ThemeGalleryExample` | The same Markdown through every bundled theme | `gallery/*.pdf` |
| `…examples.CustomBlockExample` | A custom `:::note` renderer | `custom-block.pdf` |
| `…examples.AlertsAndOutlineExample` | The five GitHub alerts + a navigable heading outline | `alerts.pdf` |
| `…examples.InPdfNavigationExample` | Clickable `[text](#heading)` links, bidirectional footnotes, inline-code chips | `in-pdf-navigation.pdf` |
| `…examples.TocExample` | A `[TOC]` marker → auto-generated, clickable table of contents nested by heading level | `toc.pdf` |
| `…examples.FooterExample` | A running footer with `Page N of M` page numbers (`FooterTokens.pageNumbers()`) | `footer.pdf` |
| `…examples.PngExportExample` | Rasterize straight to PNG page images via `toImages(dpi)` — no PDF round-trip | `png-export-p*.png` |
| `…examples.EmojiExample` | Emoji shortcodes → inline images via `ClasspathEmojiResolver` | `emoji.pdf` |
| `…examples.FrontMatterExample` | A YAML `---` front-matter title block | `front-matter.pdf` |

The emoji example bundles real **Twemoji** PNGs (CC-BY 4.0) under
[`src/main/resources/emoji/`](src/main/resources/emoji/), named by shortcode — add
more by dropping `<shortcode>.png` files there (see `ATTRIBUTION.txt`).

(`…examples` = `io.github.demchaav.markdown.examples`.)

### Render your own file

```bash
../mvnw -f pom.xml exec:java \
  -Dexec.mainClass=io.github.demchaav.markdown.examples.RenderMarkdownFileExample \
  -Dexec.args="../README.md README.pdf"
```

With no `-Dexec.args`, `RenderMarkdownFileExample` renders the bundled
[`sample.md`](src/main/resources/sample.md) to `sample.pdf`.

## See also

- [Quickstart & API](../README.md)
- [Architecture](../docs/architecture.md)
- [Theming](../docs/theming.md)
- [Custom renderers](../docs/custom-renderers.md)
