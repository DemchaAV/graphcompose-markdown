# Contributing

Thanks for your interest in graphcompose-markdown! This document covers the build, the
branch workflow and the commit conventions.

## Prerequisites

- **JDK 17+** (the build enforces a Java 17 baseline).
- No global Maven needed — use the bundled wrapper (`./mvnw`, `mvnw.cmd` on Windows).

## Build & test

```bash
./mvnw -B -ntp clean verify                    # compile + run the full test suite
./mvnw -B -ntp clean verify javadoc:javadoc    # + the Javadoc gate (run before a PR)
./mvnw -B -ntp test -Dtest=YourTest            # a single test
```

The suite includes fixture-driven smoke tests over `src/test/resources/markdown/`
(each fixture must parse, render to a non-empty multi-page PDF, and carry its expected
text). `SamplePdfGenerationTest` writes preview PDFs/PNGs into the gitignored
`preview/` directory — handy for eyeballing output locally.

## Branch workflow

```text
main      ← stable, release-tagged
  ▲
develop   ← integration branch (PRs land here)
  ▲
feature/* · fix/* · docs/* · ci/*   ← your work
```

1. Branch off `develop` (`feature/…`, `fix/…`, `docs/…`, `ci/…`).
2. Make the change; keep the build green (`./mvnw -B -ntp clean verify javadoc:javadoc`).
3. Open a PR against `develop`. CI runs the build/test matrix on JDK 17 / 21 / 25.
4. After review, the PR is squash-merged into `develop`.
5. Releases flow `develop → main` and are tagged `vX.Y.Z`.

## Commit & PR style

- **[Conventional Commits](https://www.conventionalcommits.org/)** for subjects:
  `feat(scope): …`, `fix(scope): …`, `docs: …`, `test: …`, `ci(scope): …`.
- A PR body explains **why**, then **what changed**, then **how it was verified**
  (the command you ran and the result).
- Add or update tests with behavioural changes; public API needs Javadoc.
- Note user-facing changes in [CHANGELOG.md](CHANGELOG.md) under the in-progress
  version.

## Project layout

```text
src/main/java/io/github/demchaav/markdown/
  parser/      Markdown text → Flexmark AST
  mapper/      Flexmark AST → semantic model (the decoupling boundary)
  model/       sealed MarkdownNode tree + inline nodes
  theme/       tokens, component styles, MarkdownTheme + RendererRegistry, packs
  render/      NodeRenderer SPI, RenderContext, built-in renderers
  extension/   SyntaxHighlighter SPI, ImageResolver, BundledFonts, custom-block parser
  composer/    MarkdownComposer — the public entry point
```

See [docs/architecture.md](docs/architecture.md) for how these fit together.

## License

By contributing you agree that your contributions are licensed under the
[MIT License](LICENSE).
