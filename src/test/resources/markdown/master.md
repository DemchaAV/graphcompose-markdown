# GraphCompose Markdown

A themeable Markdown document composer powered by the **GraphCompose** layout
engine.[^engine] The same Markdown can be reskinned without touching its text —
*content* and *appearance* stay separate.

## Inline formatting

Paragraphs support **bold**, *italic*, ***bold italic***, ~~strikethrough~~,
`inline code`, and [labelled links](https://github.com/DemchaAV/GraphCompose).

### Heading level three

#### Heading level four

##### Heading level five

###### Heading level six

## Lists

- Headings, paragraphs and lists
- Nested items:
    - second level
    - second level again
- Back to the first level

1. Ordered items
2. Keep their numbers
3. Across the list

Task lists track progress:

- [x] Ship GFM tables
- [x] Add theme packs
- [ ] Wire syntax highlighting

## Code

```java
// Build a themed composer and render Markdown to PDF.
var composer = MarkdownComposer.builder()
        .theme(DefaultMarkdownTheme.light())
        .build();
int pages = composer.render(markdown).writePdf(Path.of("out.pdf"));
```

A plain block carries no language and stays unhighlighted:

```
plain text, rendered verbatim
```

## Quotes and rules

> Themes decide how all of this looks — design tokens for the cosmetics,
> node renderers for the behaviour.

## Tables

| Feature     | Status | Since |
|:------------|:------:|------:|
| Headings    | stable | 0.1.0 |
| Code blocks | stable | 0.1.0 |
| Tables      | new    | 0.2.0 |

## Images

![Architecture diagram](diagram.png)

---

:::callout warning
Custom `:::` blocks render through a registered renderer. Swap the renderer to
restyle every callout at once.
:::

:::callout tip
Derive a new theme from an existing one and override only what differs.
:::

[^engine]: GraphCompose owns measurement, layout, pagination and rendering; this
library only maps Markdown onto its document model.
