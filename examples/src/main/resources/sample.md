# Sample document

A short Markdown file rendered to PDF by **graphcompose-markdown**.

## Inline formatting

Paragraphs support **bold**, *italic*, `inline code`, ~~strikethrough~~ and
[links](https://github.com/DemchaAV/graphcompose-markdown).

## Lists

- Headings, paragraphs and lists
- Nested items:
    - second level
- Back to the first level

1. Ordered items
2. Keep their numbers

- [x] Task lists work too
- [ ] Even unchecked ones

## Code

```java
var composer = MarkdownComposer.create(DefaultMarkdownTheme.light());
composer.render(markdown).writePdf(Path.of("out.pdf"));
```

## Quote

> Themes decide how all of this looks — content and appearance stay separate.

| Feature | Status |
|:--------|:------:|
| Tables  | stable |
| Themes  | stable |
