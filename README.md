# graphcompose-markdown

**Turn Markdown into beautifully themed PDFs — parsed with [Flexmark](https://github.com/vsch/flexmark-java), laid out by the [GraphCompose](https://github.com/DemchaAV/GraphCompose) engine.**

[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

graphcompose-markdown parses Markdown with **Flexmark**, maps the parse tree onto an
**independent semantic model**, and renders that model through a swappable **theme**
into the **GraphCompose** document engine — which owns measurement, layout,
pagination and PDF output.

<table>
<tr>
<td align="center"><img src="assets/readme/sample-light.png" width="380" alt="Light theme sample"><br><sub><b>DefaultMarkdownTheme.light()</b></sub></td>
<td align="center"><img src="assets/readme/sample-dark.png" width="380" alt="Dark theme sample"><br><sub><b>DefaultMarkdownTheme.dark()</b></sub></td>
</tr>
</table>

This is **not** a plain Markdown-to-PDF converter. Three concerns stay separate:

- **Content** — your Markdown text.
- **Appearance** — a theme (colors, fonts, spacing, per-element styles, renderers).
- **Layout** — GraphCompose owns measurement, pagination and output.

The same Markdown can be reskinned into completely different documents without
touching its text.

```text
Markdown ──Flexmark──▶ Flexmark AST ──mapper──▶ Semantic model ──theme + renderers──▶ GraphCompose model ──engine──▶ PDF
                                       (no Flexmark           (MarkdownNode tree)                      (layout + pagination)
                                        types downstream)
```

> Status: `0.1.0` released; `0.2.0` in development. The API may still change before `1.0.0`.

## Showcase

One Markdown document that uses **every** feature — and describes the library while doing
it — rendered straight to PDF. Open the source and the result:
**[`showcase.md`](assets/readme/showcase.md)** → **[`showcase.pdf`](assets/readme/showcase.pdf)**.

<table>
<tr>
<td align="center"><img src="assets/readme/showcase-p1.png" width="250" alt="Showcase — title block, alert, inline emoji, lists"></td>
<td align="center"><img src="assets/readme/showcase-p2.png" width="250" alt="Showcase — highlighted code, aligned table, colour-coded alerts"></td>
<td align="center"><img src="assets/readme/showcase-p4.png" width="250" alt="Showcase — an embedded reskinned render and the architecture diagram"></td>
</tr>
</table>

Front-matter title block, headings → PDF outline, inline formatting, autolinks, emoji,
nested & task lists, syntax-highlighted code, GFM tables, all five GitHub alerts,
blockquotes, footnotes, `:::` custom blocks and an embedded image — all on those pages.

## Why this library

- **Separation of content, appearance and layout.** Reskin a document by swapping a
  theme; the Markdown never changes.
- **Parser-decoupled semantic model.** Renderers operate on a sealed `MarkdownNode`
  tree, never on Flexmark types — so the parser stays swappable and you can build or
  transform the model by hand.
- **Three-layer theming.** Design tokens → component styles → node renderers. Override
  exactly what you need with `MarkdownTheme.builder(base)` and reuse everything else.
- **Composable renderer packs.** Ship and combine sets of renderers, override a single
  node type, or register a renderer for your own `:::` block type.
- **Built-in syntax highlighting** via a pluggable `SyntaxHighlighter` SPI (no extra
  dependency for the default).
- **Real PDF layout** — pagination, keep-together panels, GFM tables, vector list
  markers and footnotes, all from the GraphCompose engine.
- **No mandatory font artifact.** The default themes use the PDF base-14 fonts;
  JetBrains Mono is opt-in.
- **Multiple entry points.** Render a Markdown string, a pre-parsed Flexmark
  `Document`, or a hand-built semantic model.

## Install

> Not yet on Maven Central (first release pending). Until then, build from source
> (`./mvnw install`) and depend on the snapshot, or consume the repo via JitPack.

Maven (once released):

```xml
<dependency>
    <groupId>io.github.demchaav</groupId>
    <artifactId>graph-compose-markdown</artifactId>
    <version>0.2.0</version>
</dependency>
```

Requires **Java 17+**. The GraphCompose engine (`io.github.demchaav:graph-compose`)
comes in transitively.

## Quickstart

```java
import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import java.nio.file.Path;

String md = """
        # Release notes

        GraphCompose **1.9** ships *themeable* Markdown rendering.

        - Headings, lists and `inline code`
        - Syntax-highlighted code blocks
        - [Links](https://github.com/DemchaAV/GraphCompose)

        > Themes decide how all of this looks.
        """;

MarkdownComposer composer = MarkdownComposer.create(DefaultMarkdownTheme.light());

composer.render(md).writePdf(Path.of("release-notes.pdf"));
// or: byte[] pdf = composer.render(md).toPdfBytes();
//     composer.render(md).writePdf(outputStream);

// Render a Markdown *file* directly — reads UTF-8 and resolves relative images
// (e.g. ![](diagram.png)) against the file's own folder:
composer.renderFile(Path.of("docs/guide.md")).writePdf(Path.of("guide.pdf"));
```

## Usage at a glance

One composer, four ways in, three ways out. A composer is immutable and thread-safe —
build it once and reuse it.

| Take this in | Call | Notes |
|---|---|---|
| A Markdown **string** | `composer.render(String)` | `null` is treated as empty |
| A Markdown **file** | `composer.renderFile(Path)` | reads UTF-8; resolves relative images (`![](pic.png)`) against the file's own folder; throws `IOException` |
| A parsed Flexmark **`Document`** | `composer.render(Document)` | bring your own parser/extensions; `:::` blocks are *not* extracted on this path |
| A hand-built **`MarkdownDocument`** | `composer.render(MarkdownDocument)` | the stable semantic-model hand-off point |

Each returns a `Rendered`, which writes the PDF three ways (all throw `DocumentRenderingException`):

| Get this out | Call |
|---|---|
| Write to a **file** | `rendered.writePdf(Path)` |
| Stream to an **`OutputStream`** | `rendered.writePdf(OutputStream)` |
| In-memory **`byte[]`** | `rendered.toPdfBytes()` |

```java
MarkdownComposer composer = MarkdownComposer.create(GitHubTheme.dark());
composer.renderFile(Path.of("doc.md")).writePdf(Path.of("doc.pdf"));   // file → PDF
byte[] bytes = composer.render("# Hello").toPdfBytes();                 // string → bytes
```

Pick the look by passing a theme to `create(...)`: `DefaultMarkdownTheme.light()` / `.dark()`,
or a ready-made pack from `io.github.demchaav.markdown.theme.packs` — `GitHubTheme`,
`AcademicTheme`, `MinimalTheme`, `BusinessReportTheme`. No Java at all? The
[`gcmd` CLI](#command-line-cli) renders a file straight from the shell.

## Command-line (CLI)

A standalone `cli/` module renders Markdown to PDF from the shell — no Java code
required. Build the executable fat-jar (it bundles every dependency):

```bash
./mvnw -q -ntp install -DskipTests        # install the library to ~/.m2
./mvnw -f cli/pom.xml -q -ntp package     # -> cli/target/graph-compose-markdown-cli.jar
```

Then render:

```bash
# basic: writes README.pdf next to the input
java -jar cli/target/graph-compose-markdown-cli.jar README.md

# pick a theme, an output path, an image base dir and an emoji dir
java -jar cli/target/graph-compose-markdown-cli.jar docs/guide.md \
    -o build/guide.pdf -t github-dark -i docs/assets -e docs/emoji

# read from stdin
cat notes.md | java -jar cli/target/graph-compose-markdown-cli.jar - -o notes.pdf
```

| Option | Meaning |
|---|---|
| `INPUT` | Markdown file, or `-` for stdin |
| `-o, --output FILE` | Output PDF, or `-` to write the PDF to stdout (default: input name with `.pdf`, or `out.pdf` for stdin) |
| `-t, --theme NAME` | `light` (default), `dark`, `github-light`, `github-dark`, `academic`, `minimal`, `business` |
| `-i, --images DIR` | Base dir for relative image paths (default: the input file's dir) |
| `-e, --emoji DIR` | Dir of `<shortcode>.png` files to render `:shortcode:` inline |
| `--mono-jetbrains` | Render code in bundled JetBrains Mono |
| `--strict` | Fail on unsupported Markdown instead of degrading |
| `-h, --help` / `-V, --version` | Usage / version |

### Shortcut: the `gcmd` wrapper

The module ships launcher scripts that find the jar next to themselves, so you can
drop the `java -jar …` boilerplate:

```bash
cli/gcmd README.md -t github-dark        # Git Bash / WSL / macOS / Linux
cli\gcmd.cmd README.md -t github-dark     # Windows (cmd / PowerShell)
```

Put the `cli/` folder on your `PATH` (or symlink/copy the matching `gcmd` script into
a directory already on it) to call `gcmd` from anywhere. The wrappers print a build
hint if the jar hasn't been built yet.

The CLI module is standalone (outside the main build), so it never affects the
published library artifact.

## What renders today

Headings (h1–h6), paragraphs with inline **bold** / *italic* / ~~strikethrough~~ /
`inline code` (rendered on a rounded GitHub-style chip) / links (plus **bare-URL
autolinking** and clickable **`[text](#heading)` internal links**), ordered & unordered
(nested) lists, **task lists**,
**syntax-highlighted** fenced code blocks, blockquotes, horizontal rules, images,
**GFM tables** (with per-column alignment), **footnotes** (clickable & bidirectional),
**GitHub-style alerts**
(`> [!NOTE]` / `[!TIP]` / `[!IMPORTANT]` / `[!WARNING]` / `[!CAUTION]`), **emoji
shortcodes** (`:rocket:`), **YAML front matter** (a `---` … `---` title block), and
`:::` custom blocks (e.g. callouts).

Emoji shortcodes render as **inline images** when an `EmojiResolver` is configured
(e.g. `ClasspathEmojiResolver` pointed at a classpath folder of `<shortcode>.png` files
you supply, such as Twemoji renamed by shortcode — the library bundles no emoji images);
otherwise they fall back to readable `:shortcode:` text, since PDF fonts carry no emoji
glyphs. **Geometric emoji**
typed literally — coloured circles `🔴🟢🟡`, squares `🟥🟩`, `🔺`, diamonds `🔶🔷` and
stars `⭐` — render as **native vector shapes** in their own colour (no font, no image),
instead of a missing-glyph `?`.

Headings also become a **navigable PDF outline** — the viewer's bookmark/outline pane
mirrors the document's heading tree — and each declares a **GitHub-style anchor**, so
`[text](#heading)` links jump straight to it as native PDF go-to actions (footnote markers
jump to their note and back the same way). A standalone **`[TOC]`** (or `[[_TOC_]]`) line
expands into an **auto-generated, clickable table of contents** — one link per heading,
nested by level — that you can drop anywhere, including above the headings it lists.

Content the library does not model (raw HTML blocks, inline HTML) is **surfaced as raw
text rather than silently dropped**; `MarkdownComposer.builder().strictMode(true)`
rejects such a document instead, for pipelines that must fail loudly.

## Architecture

```text
MarkdownComposer.render(String)
   │  Flexmark parser (+ GFM tables, task lists, strikethrough, footnotes;
   │                    emoji, autolink, YAML front matter)
   ▼
Flexmark AST
   │  FlexmarkAstMapper  — the boundary: nothing downstream imports Flexmark
   ▼
MarkdownDocument  (sealed MarkdownNode tree: HeadingNode, ParagraphNode, ListNode,
   │               CodeBlockNode, QuoteNode, TableNode, CustomBlockNode, …)
   │  RendererRegistry  — one NodeRenderer per node type, from the theme
   ▼
GraphCompose document model  (sections, paragraphs, RichText, tables, panels)
   │  GraphCompose engine
   ▼
Layout + pagination → PDF
```

The semantic model is the stable hand-off point: the parser is swappable, renderers
never see Flexmark, and you can construct or transform the model directly. See
**[docs/architecture.md](docs/architecture.md)** for the full picture.

## Theming

A `MarkdownTheme` is built from three layers, so you change exactly as much as you
need and reuse everything else:

1. **Design tokens** (`MarkdownTokens`) — cosmetic values: colors, fonts, sizes,
   spacing, borders, corner radii, page geometry, syntax-highlight colors.
2. **Component styles** (`MarkdownStyles`) — per-element styles (`CodeBlockStyle`,
   `ListStyle`, `QuoteStyle`, `CalloutStyle`, …) derived from tokens; headings use an
   `InlineStyle` via `headingInline(level)`.
3. **Node renderers** (`NodeRenderer`) — the behaviour that turns each semantic node
   into GraphCompose builders, bound to node types in a `RendererRegistry`.

```java
MarkdownTheme base = DefaultMarkdownTheme.light();

MarkdownTheme custom = MarkdownTheme.builder(base)
        // layer 1 — reskin a cosmetic token
        .tokens(base.tokens().withColors(
                base.tokens().colors().withCodeBackground(DocumentColor.rgb(246, 248, 250))))
        // layer 3 — swap one renderer, reuse every other component
        .renderer(CodeBlockNode.class, new LabeledCodeBlockRenderer())
        .build();
```

### Ready-made theme packs

Beyond `DefaultMarkdownTheme.light()` / `.dark()`, the
`io.github.demchaav.markdown.theme.packs` package ships drop-in themes — the same
Markdown, reskinned:

<table>
<tr>
<td align="center"><img src="assets/readme/theme-github-light.png" width="200" alt="GitHub light"><br><sub><code>GitHubTheme.light()</code></sub></td>
<td align="center"><img src="assets/readme/theme-github-dark.png" width="200" alt="GitHub dark"><br><sub><code>GitHubTheme.dark()</code></sub></td>
<td align="center"><img src="assets/readme/theme-academic.png" width="200" alt="Academic"><br><sub><code>AcademicTheme.light()</code></sub></td>
</tr>
<tr>
<td align="center"><img src="assets/readme/theme-minimal.png" width="200" alt="Minimal"><br><sub><code>MinimalTheme.light()</code></sub></td>
<td align="center"><img src="assets/readme/theme-business-report.png" width="200" alt="Business report"><br><sub><code>BusinessReportTheme.light()</code></sub></td>
<td align="center"><img src="assets/readme/theme-default-light.png" width="200" alt="Default light"><br><sub><code>DefaultMarkdownTheme.light()</code></sub></td>
</tr>
</table>

```java
MarkdownComposer.create(GitHubTheme.dark()).render(md).writePdf(path);
```

Full theming guide: **[docs/theming.md](docs/theming.md)**.

## Custom renderers — change *how* Markdown renders

A `NodeRenderer` is a single method that turns one semantic node into GraphCompose
builders, reading all styling from the `RenderContext`:

```java
NodeRenderer<CodeBlockNode> labeled = (node, host, ctx) -> {
    // emit GraphCompose builders into `host`; read styling from `ctx`
    host.addParagraph(p -> p.text(node.language().toUpperCase()));
    // ...render the code body using ctx.styles(), ctx.highlighter(), …
};
```

Bundle renderers into a **pack**, override a single node type, or register a renderer
for your own `:::` block — reusing everything else:

```java
MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
        .pack(new MyAlertsPack())                   // bundle renderers from another source
        .renderer(CodeBlockNode.class, labeled)     // override one node renderer
        .customBlock("chart", new ChartRenderer())  // render your own ::: block type
        .build();
```

A `:::chart … :::` block routes to the renderer registered for `"chart"`; any
unrecognised `:::` type falls back to the callout style. Step-by-step guide (with a
full custom `RendererPack`): **[docs/custom-renderers.md](docs/custom-renderers.md)**.

### Syntax highlighting

Code highlighting uses a pluggable `SyntaxHighlighter` SPI. The built-in
`RegexSyntaxHighlighter` covers ~15 common languages with no extra dependency; plug a
grammar-based highlighter via `MarkdownTheme.builder().highlighter(...)`. Colors come
from the theme's `SyntaxColors` token group (light/dark palettes).

### Rich fonts (optional)

The default themes use the PDF **base-14** fonts (Helvetica / Times / Courier). To
render code in **JetBrains Mono**, add the bundled-fonts artifact and upgrade any
theme with `BundledFonts`:

```xml
<dependency>
    <groupId>io.github.demchaav</groupId>
    <artifactId>graph-compose-fonts</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
MarkdownTheme theme = BundledFonts.jetBrainsMonoCode(DefaultMarkdownTheme.light());
```

<p align="center"><img src="assets/readme/sample-jetbrains.png" width="420" alt="Code rendered in JetBrains Mono"></p>

The dependency is declared `optional`, so it only ships if you ask for it.

## Bring your own AST

Already have a Flexmark tree (parsed with your own parser and extensions), or build
the semantic model yourself? Render either directly — no string round-trip, no file:

```java
// a com.vladsch.flexmark.util.ast.Document you already parsed
composer.render(flexmarkDocument).toPdfBytes();

// or a hand-built / transformed MarkdownDocument semantic model
composer.render(markdownDocument).writePdf(out);
```

(The `:::` custom-block extraction is a text-level pre-pass, so it only runs for the
`render(String)` entry point.)

## Examples

Runnable examples live in [`examples/`](examples/) — render an inline string,
**read a Markdown file and write a PDF**, render the same content through every theme,
or wire a custom `:::` block renderer. Build the library once, then run one:

```bash
./mvnw -B -ntp -DskipTests install        # install the library into your local Maven repo
cd examples && ../mvnw exec:java \
  -Dexec.mainClass=io.github.demchaav.markdown.examples.RenderMarkdownFileExample \
  -Dexec.args="../README.md README.pdf"
```

See [examples/README.md](examples/README.md) for the full list.

## Documentation

- **[Architecture](docs/architecture.md)** — the pipeline, the semantic model, and why
  the parser is decoupled.
- **[Theming](docs/theming.md)** — tokens, component styles, deriving themes, packs,
  syntax colors, rich fonts.
- **[Custom renderers](docs/custom-renderers.md)** — write a `NodeRenderer`, a
  `RendererPack`, and custom `:::` block types.
- **[Changelog](CHANGELOG.md)** — release notes.

## Building from source

```bash
./mvnw -B -ntp clean verify          # compile + run the full test suite
./mvnw -B -ntp clean verify javadoc:javadoc   # + Javadoc gate
```

See **[CONTRIBUTING.md](CONTRIBUTING.md)** for the branch workflow and commit style.

## License

[MIT](LICENSE) © Artem Demchyshyn
