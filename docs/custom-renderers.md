# Custom renderers

A `NodeRenderer` decides *how* one kind of semantic node becomes a GraphCompose
document fragment. Override one to restyle a single element; bundle a set into a
`RendererPack` to ship a whole behaviour layer; register one against a `:::` type to
render your own block.

## The contract

```java
@FunctionalInterface
public interface NodeRenderer<N extends MarkdownNode> {
    void render(N node, SectionBuilder host, RenderContext ctx);
}
```

- `node` — the semantic node (a sealed `MarkdownNode` subtype, never a Flexmark type).
- `host` — the GraphCompose `SectionBuilder` to emit into (`addParagraph`, `addRich`,
  `addList`, `addTable`, `addLine`, `addImage`, `addSection`, …).
- `ctx` — the `RenderContext`: everything you need to style and to render children.

A renderer **emits builders**; it never draws to a page or computes geometry. The
engine handles measurement, line breaking and pagination.

## The `RenderContext` API

| Method | Returns | Use |
|--------|---------|-----|
| `styles()` | `MarkdownStyles` | per-element component styles (derived from tokens) |
| `tokens()` | `MarkdownTokens` | raw design tokens |
| `toRich(nodes, base)` | `RichText` | convert inline runs → GraphCompose `RichText` |
| `paragraphInline()` / `headingInline(level)` | `InlineStyle` | base inline style for body / a heading level |
| `renderBlock(node, host)` | `void` | dispatch one child block through the registry |
| `renderBlocks(nodes, host)` | `void` | dispatch a list of child blocks |
| `highlighter()` | `SyntaxHighlighter` | tokenize code for highlighting |
| `images()` | `ImageResolver` | resolve image sources |
| `withTextColor(color)` | `RenderContext` | a context whose inline text defaults to `color` |

**Always read styling from `ctx`, never hard-code values** — that is what lets a token
change cascade across the whole theme.

## Example: a labelled code block

Override `CodeBlockNode` to add a language label bar above the panel, reusing the
theme's code styling:

```java
NodeRenderer<CodeBlockNode> labelled = (node, host, ctx) -> {
    if (!node.language().isBlank()) {
        host.addParagraph(p -> p.text(node.language().toUpperCase())
                .textStyle(/* a small muted style from ctx.styles()/tokens() */));
    }
    // delegate the body to the built-in behaviour, or emit your own using
    // ctx.highlighter() + ctx.styles().syntaxColor(type)
};

MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
        .renderer(CodeBlockNode.class, labelled)
        .build();
```

## Example: a container that renders children

Container nodes recurse through the context. A blockquote-like renderer:

```java
NodeRenderer<QuoteNode> quote = (node, host, ctx) -> host.addSection(panel -> {
    panel.accentLeft(/* bar color from ctx.tokens() */);
    panel.padding(/* from ctx.styles() */);
    ctx.renderBlocks(node.content(), panel);    // ← child blocks dispatch back through the registry
});
```

## Renderer packs

A `RendererPack` bundles renderers so a project can ship and compose its own set:

```java
public interface RendererPack {
    void registerInto(RendererRegistry registry);
}
```

`StandardPack` registers every built-in renderer. Apply packs in order (later packs
override earlier bindings for the same node type), then override individuals:

```java
MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
        .pack(new MyAlertsPack())                 // bundle from another source
        .renderer(CodeBlockNode.class, labelled)  // override one node type
        .build();
```

```java
public final class MyAlertsPack implements RendererPack {
    @Override public void registerInto(RendererRegistry registry) {
        registry.register(QuoteNode.class, new FancyQuoteRenderer());
        registry.registerCustomBlock("note", new NoteRenderer());
    }
}
```

## Custom `:::` block types

A fenced custom block —

```markdown
:::chart bar
revenue, 120
margin, 22
:::
```

— is parsed into a `CustomBlockNode(String type, String variant, List<MarkdownNode> content)`
(here `type = "chart"`, `variant = "bar"`, and the body lines are the nested
`content`). Register a renderer for a specific `type`:

```java
MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
        .customBlock("chart", new ChartRenderer())   // type-specific
        .build();
```

`.customBlock(type, renderer)` (and `RendererRegistry.registerCustomBlock(type, r)`)
dispatch by the block's `type`. Any unregistered `:::` type falls back to the built-in
**callout** style, so `:::callout warning` works out of the box. Custom-block
extraction is a text-level pre-pass, so it runs only for `render(String)` (not for the
bring-your-own-AST entry points).

> The semantic model is **sealed**, so a `:::` block (`CustomBlockNode`) is the only way to
> introduce a block type of your own. You can override or replace the renderer for any existing
> node type, but you cannot add a new `MarkdownNode` subtype — the `NodeRenderer<N>` /
> `MarkdownTheme.Builder.renderer(Class<N>, …)` generics bind to the existing node classes, not
> to types you define.

## Syntax highlighting

Code highlighting is a pluggable SPI:

```java
@FunctionalInterface
public interface SyntaxHighlighter {
    List<CodeToken> highlight(String code, String language);
}
```

The default `RegexSyntaxHighlighter` covers ~15 common languages with no extra
dependency. To plug a grammar-based engine (TextMate, a real lexer, …), implement the
interface and set it on the theme:

```java
MarkdownTheme theme = MarkdownTheme.builder(DefaultMarkdownTheme.light())
        .highlighter(new MyTextMateHighlighter())
        .build();
```

Each returned `CodeToken` carries a `CodeTokenType`; the renderer colors it via
`ctx.styles().syntaxColor(type)`, which resolves to the theme's `SyntaxColors`. The
concatenation of all token texts must equal the input verbatim (the renderer relies on
this to preserve whitespace and indentation).

See also: [architecture.md](architecture.md) · [theming.md](theming.md)
