package io.github.demchaav.markdown.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * YAML front matter — the {@code ---} … {@code ---} metadata block at the very top of a
 * document. {@code values} maps each key to its list of values (YAML keys can repeat).
 *
 * <p>The default renderer draws a title block from the well-known keys ({@code title},
 * {@code subtitle}, {@code author}, {@code date}); other keys are carried here for
 * programmatic use but not rendered.</p>
 *
 * @param values the parsed metadata, key → values (immutable)
 */
public record FrontMatterNode(Map<String, List<String>> values) implements MarkdownNode {

    /**
     * Creates a front-matter node, copying the metadata defensively.
     *
     * @param values the parsed metadata; must not be {@code null}
     */
    public FrontMatterNode {
        Objects.requireNonNull(values, "values");
        Map<String, List<String>> copy = new LinkedHashMap<>();
        values.forEach((key, list) -> copy.put(key, List.copyOf(list)));
        values = Map.copyOf(copy);
    }

    /**
     * Returns the first value for a key, or {@code null} if the key is absent or empty.
     *
     * @param key the metadata key (e.g. {@code "title"})
     * @return the first value, or {@code null}
     */
    public String first(String key) {
        List<String> list = values.get(key);
        return list == null || list.isEmpty() ? null : list.get(0);
    }
}
