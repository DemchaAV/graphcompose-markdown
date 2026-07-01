package io.github.demchaav.markdown.render;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** GitHub-compatible heading-slug rules used for anchors and {@code #fragment} link resolution. */
class SlugsTest {

    @Test
    void slugifiesLikeGitHub() {
        assertThat(Slugs.slugify("My Heading")).isEqualTo("my-heading");
        assertThat(Slugs.slugify("Hello, World!")).isEqualTo("hello-world");
        assertThat(Slugs.slugify("  Spaced   Out  ")).isEqualTo("spaced-out");
        assertThat(Slugs.slugify("Section 1")).isEqualTo("section-1");
        assertThat(Slugs.slugify("snake_case-kept")).isEqualTo("snake_case-kept");
    }

    @Test
    void textWithNoSlugableCharactersIsEmpty() {
        assertThat(Slugs.slugify("###")).isEmpty();
        assertThat(Slugs.slugify("  ")).isEmpty();
        assertThat(Slugs.slugify("")).isEmpty();
        assertThat(Slugs.slugify(null)).isEmpty();
    }
}
