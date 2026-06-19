# graphcompose-markdown

**A themeable Markdown document composer powered by the [GraphCompose](https://github.com/DemchaAV/GraphCompose) layout engine.**

This is **not** a plain Markdown-to-PDF converter. Markdown carries the *content*;
a **theme** carries the *appearance*; **GraphCompose** owns measurement, layout,
pagination and output. The same Markdown can be reskinned into completely
different documents without touching its text.

```text
Markdown
   ↓  Flexmark
Flexmark AST
   ↓  mapper
Semantic Markdown tree (independent of Flexmark)
   ↓  theme + renderers
GraphCompose document model
   ↓  GraphCompose engine
Layout + pagination → PDF
```

> Status: early preview (`0.1.0-SNAPSHOT`). API may change before `1.0.0`.

## Install

Maven:

```xml
<dependency>
    <groupId>io.github.demchaav</groupId>
    <artifactId>graph-compose-markdown</artifactId>
    <version>0.1.0</version>
</dependency>
```

Requires **Java 17+**. The GraphCompose engine (`io.github.demchaav:graph-compose`)
is pulled in transitively.

## Quickstart

```java
import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import java.nio.file.Path;

String md = """
        # Release notes

        GraphCompose **1.8** ships *themeable* Markdown rendering.

        - Headings, lists and `inline code`
        - Fenced code blocks
        - [Links](https://github.com/DemchaAV/GraphCompose)

        > Themes decide how all of this looks.
        """;

MarkdownComposer composer = MarkdownComposer.builder()
        .theme(DefaultMarkdownTheme.light())
        .build();

composer.render(md).writePdf(Path.of("release-notes.pdf"));
// or: byte[] pdf = composer.render(md).toPdfBytes();
```

## Theming

A `MarkdownTheme` is built from three layers, so you change exactly as much as
you need and reuse everything else:

1. **Design tokens** (`MarkdownTokens`) — pure cosmetic values: colors, fonts,
   font sizes, spacing, borders, corner radii, page geometry. Swap tokens to
   reskin the whole document.
2. **Component styles** — per-element styles (`HeadingStyle`, `CodeBlockStyle`,
   `ListStyle`, `QuoteStyle`, …) derived from tokens but individually overridable.
3. **Node renderers** (`NodeRenderer`) — the behaviour that turns each semantic
   node into GraphCompose builders (spacing, container shape, list markers, child
   layout). Bound to node types in a `RendererRegistry`.

Compose a new theme from an existing one — `builder(base)` copies its tokens,
renderers and image resolver, so you override only what differs:

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

A `NodeRenderer` is a single method, so a custom one is small:

```java
NodeRenderer<CodeBlockNode> labeled = (node, host, ctx) -> {
    // ...emit GraphCompose builders into `host`, reading styling from `ctx`...
};
```

### Ready-made theme packs

Beyond `DefaultMarkdownTheme.light()` / `.dark()`, the
`io.github.demchaav.markdown.theme.packs` package ships drop-in themes:

| Pack | Look |
|------|------|
| `GitHubTheme.light()` / `.dark()` | GitHub Primer palette, sans + monospace |
| `AcademicTheme.light()` | serif body, generous leading, wide margins |
| `MinimalTheme.light()` | monochrome, hairline rules, lots of whitespace |
| `BusinessReportTheme.light()` | serif headings over a sans body, navy + teal |

```java
MarkdownComposer.create(GitHubTheme.dark()).render(md).writePdf(path);
```

## What renders today

Headings (h1–h6), paragraphs with inline **bold** / *italic* / ~~strikethrough~~ /
`inline code` / links, ordered & unordered (nested) lists, **task lists**, fenced
code blocks, blockquotes, horizontal rules, images, **GFM tables** (with
per-column alignment), and `:::` custom blocks (e.g. callouts).

Planned: footnotes, syntax highlighting, and a DOCX backend (the engine already
supports it).

## License

[MIT](LICENSE) © Artem Demchyshyn
