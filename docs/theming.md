# Theming

A `MarkdownTheme` decides how a document looks. It is built from three layers so you
override exactly what you need and reuse everything else.

```text
MarkdownTokens   (cosmetic values)
      │  derives
MarkdownStyles   (per-element component styles)
      │  read by
NodeRenderer…    (behaviour, in a RendererRegistry)
```

## Layer 1 — design tokens (`MarkdownTokens`)

Pure cosmetic values, grouped:

| Group | Holds |
|-------|-------|
| `ColorTokens` | text, muted text, accent, links, surface (the page background), code background, quote bar, rules, table row background |
| `TypographyTokens` | body / heading / code font families, body & code sizes, line spacing, the six heading sizes |
| `SpacingTokens` | block gaps, paddings, table cell padding |
| `ShapeTokens` | corner radii, border/line weights |
| `PageTokens` | page size, margins, content width |
| `SyntaxColors` | code highlight colors (keyword, string, comment, number, annotation, function) |

Swap a token group to reskin everything that derives from it. Tokens are immutable
records with `withX` copy methods:

```java
MarkdownTokens tokens = base.tokens()
        .withColors(base.tokens().colors().withAccent(DocumentColor.rgb(0, 120, 212)))
        .withSyntax(SyntaxColors.defaultDark());
```

## Layer 2 — component styles (`MarkdownStyles`)

`MarkdownStyles` derives per-element styles (`CodeBlockStyle`, `QuoteStyle`,
`ListStyle`, `RuleStyle`, `CalloutStyle`, `TableStyle`) from the tokens; headings use an
`InlineStyle` via `headingInline(level)`. Renderers read styling exclusively from here
(via the `RenderContext`), never from literal values — so a token change cascades
everywhere automatically.

## Layer 3 — node renderers

The behaviour layer. See [custom-renderers.md](custom-renderers.md).

## Building a theme

### Derive from an existing theme (recommended)

`MarkdownTheme.builder(base)` copies the base theme's tokens, renderers, image
resolver, syntax highlighter and registered fonts — you override only what differs:

```java
MarkdownTheme custom = MarkdownTheme.builder(DefaultMarkdownTheme.light())
        .tokens(base.tokens().withColors(myColors))   // reskin tokens
        .renderer(CodeBlockNode.class, myCodeRenderer) // swap one renderer
        .build();
```

### From scratch

`MarkdownTheme.builder()` starts empty. Supply tokens and a set of renderers (usually
via `.pack(new StandardPack())` to get all the built-ins), then override:

```java
MarkdownTheme theme = MarkdownTheme.builder()
        .tokens(myTokens)
        .pack(new StandardPack())
        .build();
```

`DefaultMarkdownTheme.of(MarkdownTokens)` is a shortcut: a token bundle plus the
standard renderers.

## Ready-made theme packs

`io.github.demchaav.markdown.theme.packs`:

| Pack | Look |
|------|------|
| `GitHubTheme.light()` / `.dark()` | GitHub Primer palette, sans + monospace |
| `AcademicTheme.light()` | serif body, generous leading, wide margins |
| `MinimalTheme.light()` | monochrome, hairline rules, lots of whitespace |
| `BusinessReportTheme.light()` | serif headings over a sans body, navy + teal |

```java
MarkdownComposer.create(GitHubTheme.dark()).render(md).writePdf(path);
```

## Syntax-highlight colors

`SyntaxColors` is a token group with `defaultLight()` and `defaultDark()` palettes.
Dark themes use the dark palette; everything else defaults to light. Override per
theme:

```java
MarkdownTokens t = base.tokens().withSyntax(SyntaxColors.defaultDark());
```

The token kind → color mapping is exposed via `MarkdownStyles.syntaxColor(type)`. To
change *which* spans get colored (the tokenizer), swap the highlighter — see
[custom-renderers.md](custom-renderers.md#syntax-highlighting).

## Rich fonts (optional)

The default themes use the PDF base-14 fonts, so the core needs no font artifact. To
render code in JetBrains Mono, add `io.github.demchaav:graph-compose-fonts` (declared
`optional`) and upgrade a theme:

```java
MarkdownTheme theme = BundledFonts.jetBrainsMonoCode(DefaultMarkdownTheme.light());
```

`BundledFonts` registers the font family into the render session (the theme carries a
list of `FontFamilyDefinition`s) and switches the code token family to
`FontFamily.MONO_JETBRAINS`. Body and headings are untouched. Because bundled fonts
resolve weight/slant from the registered source set rather than from distinct font
names, the rich code path is intended for regular-weight code.

## Images

`MarkdownTheme.builder().imageResolver(...)` sets the `ImageResolver` SPI used to load
`![alt](src)` sources (the composer uses the theme's resolver). The default resolves
local and classpath paths; network fetching is opt-in (implement your own resolver).

See also: [architecture.md](architecture.md) · [custom-renderers.md](custom-renderers.md)
