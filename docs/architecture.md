# Architecture

graphcompose-markdown is a pipeline of small, decoupled stages. Each stage hands the
next a stable data structure, so any stage can be swapped or driven directly.

```text
MarkdownComposer.render(String)
   │  Flexmark parser  (+ GFM tables, task lists, strikethrough, footnotes;
   │                     emoji, autolink, YAML front matter)
   ▼
Flexmark AST                       com.vladsch.flexmark.util.ast.Document
   │  FlexmarkAstMapper            ◀── the boundary: nothing below imports Flexmark
   ▼
MarkdownDocument                   a sealed MarkdownNode tree (the semantic model)
   │  RendererRegistry             one NodeRenderer per node type, supplied by the theme
   ▼
GraphCompose document model        sections, paragraphs, RichText, tables, panels
   │  GraphCompose engine
   ▼
Layout + pagination → PDF
```

## 1. Parsing (Flexmark)

Markdown text is parsed by [Flexmark](https://github.com/vsch/flexmark-java) with the
GFM extensions enabled (tables, task lists, strikethrough, footnotes) plus emoji
shortcodes, bare-URL autolinking, and YAML front matter. Flexmark is an implementation
detail — it never leaks past the next stage (with one opt-in exception: the
`render(com.vladsch.flexmark.util.ast.Document)` overload, for callers who already hold a
parsed Flexmark tree).

You can skip this stage: `MarkdownComposer.render(Document)` accepts a Flexmark
`Document` you parsed yourself (with whatever parser options and extensions you want).

## 2. The mapper boundary (`FlexmarkAstMapper`)

`FlexmarkAstMapper` translates the Flexmark AST into the library's own **semantic
model**. This is the single most important design decision: it is the boundary that
keeps the rest of the library free of Flexmark types.

```java
public sealed interface MarkdownNode
        permits HeadingNode, ParagraphNode, ListNode, CodeBlockNode, QuoteNode,
                ThematicBreakNode, ImageNode, TableNode, CustomBlockNode, AlertNode,
                FrontMatterNode, FootnotesNode, UnsupportedBlockNode {
}
```

Inline content is its own sealed hierarchy (`InlineNode`: text, code, strong,
emphasis, strikethrough, link, image, line break, footnote reference, emoji, and an
unsupported-inline run). The lists above are the source of truth — see
[`MarkdownNode`](../src/main/java/io/github/demchaav/markdown/model/MarkdownNode.java)
and [`InlineNode`](../src/main/java/io/github/demchaav/markdown/model/inline/InlineNode.java).

Because the model is sealed and parser-independent:

- **Renderers never see Flexmark.** They are typed on `MarkdownNode` subtypes, so the
  parser can be replaced without touching a single renderer.
- **You can build the model by hand.** `MarkdownComposer.render(MarkdownDocument)`
  renders a model you constructed or transformed programmatically — no Markdown text
  required.
- **Switches are exhaustive.** Sealed types let renderers and tests cover every case.

## 3. Rendering (the theme)

A `MarkdownTheme` carries everything needed to turn the semantic model into a
GraphCompose document. It has three layers (see [theming.md](theming.md)):

| Layer | Type | Responsibility |
|-------|------|----------------|
| Design tokens | `MarkdownTokens` | cosmetic values — colors, fonts, sizes, spacing, shapes, page geometry, syntax colors |
| Component styles | `MarkdownStyles` | per-element styles derived from tokens (`CodeBlockStyle`, `QuoteStyle`, `CalloutStyle`, …; headings use an `InlineStyle` via `headingInline(level)`) |
| Node renderers | `NodeRenderer` in a `RendererRegistry` | how each node becomes GraphCompose builders |

Rendering walks the `MarkdownDocument` block by block. For each node, the
`RendererRegistry` looks up the `NodeRenderer` bound to that node's class and calls it
with a `RenderContext` (the theme's styles, tokens, inline renderer, image resolver
and syntax highlighter). Container nodes (quotes, callouts) render their children by
calling back into the context:

```java
ctx.renderBlocks(node.content(), host);    // dispatch child blocks through the registry
ctx.toRich(node.content(), baseStyle);     // convert inline runs to GraphCompose RichText
```

## 4. Layout & output (GraphCompose)

Renderers emit GraphCompose **builders** (sections, paragraphs, `RichText`, tables,
rounded panels) — they never draw to a page or compute coordinates. The GraphCompose
engine owns measurement, line breaking, pagination, keep-together behaviour and the
final PDF (or other backend) output.

The composer wires the theme's page geometry and any bundled fonts into a
`DocumentSession`, then asks it for bytes, a file, or a stream:

```java
composer.render(md).toPdfBytes();
composer.render(md).writePdf(Path.of("out.pdf"));
composer.render(md).writePdf(outputStream);
```

## Unsupported content & strict mode

A document engine must not lose content silently. When the mapper meets a block or
inline element it does not model (a raw HTML block, unmodelled inline HTML, …) it does
**not** drop it — it preserves the original source in an `UnsupportedBlockNode` /
`UnsupportedInlineRun`, which a renderer surfaces as raw text. For pipelines that would
rather fail than emit unfaithful output, `MarkdownComposer.builder().strictMode(true)`
rejects such a document with `UnsupportedMarkdownException` instead.

## Why decouple the parser?

A plain Markdown→PDF converter bakes the parser, the styling and the layout into one
pass. That makes it impossible to reskin without editing content, to reuse a renderer
across looks, or to feed in a model from somewhere other than Markdown text. Splitting
the pipeline at the semantic model buys all three — at the cost of one mapping stage.

See also: [theming.md](theming.md) · [custom-renderers.md](custom-renderers.md)
