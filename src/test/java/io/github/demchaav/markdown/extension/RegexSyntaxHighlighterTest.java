package io.github.demchaav.markdown.extension;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.demchaav.markdown.extension.CodeTokenType.COMMENT;
import static io.github.demchaav.markdown.extension.CodeTokenType.KEYWORD;
import static io.github.demchaav.markdown.extension.CodeTokenType.NUMBER;
import static io.github.demchaav.markdown.extension.CodeTokenType.PLAIN;
import static io.github.demchaav.markdown.extension.CodeTokenType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

class RegexSyntaxHighlighterTest {

    private final RegexSyntaxHighlighter highlighter = new RegexSyntaxHighlighter();

    private static String concat(List<CodeToken> tokens) {
        return tokens.stream().map(CodeToken::text).collect(Collectors.joining());
    }

    @Test
    void tokensConcatenateBackToTheOriginalCode() {
        String code = "int x = 0xFF;   // hex\nString s = \"a\\\"b\";\n@Override void f() {}\n";
        for (String language : List.of("java", "javascript", "python", "sql", "go", "rust", "csharp", "cpp", "xml")) {
            assertThat(concat(highlighter.highlight(code, language)))
                    .as("round-trips for %s", language)
                    .isEqualTo(code);
        }
    }

    @Test
    void classifiesJavaKeywordsNumbersAndComments() {
        List<CodeToken> tokens = highlighter.highlight("int answer = 42; // the answer\n", "java");

        assertThat(tokens).anySatisfy(t -> {
            assertThat(t.type()).isEqualTo(KEYWORD);
            assertThat(t.text()).isEqualTo("int");
        });
        assertThat(tokens).anySatisfy(t -> assertThat(t.type()).isEqualTo(NUMBER));
        assertThat(tokens).anySatisfy(t -> assertThat(t.type()).isEqualTo(COMMENT));
    }

    @Test
    void keywordsInsideAStringAreNotColoured() {
        List<CodeToken> tokens = highlighter.highlight("var s = \"if for while\";", "java");

        assertThat(tokens).anySatisfy(t -> {
            assertThat(t.type()).isEqualTo(STRING);
            assertThat(t.text()).contains("if for while");
        });
        // Only the real keyword ("var") is a keyword; if/for/while live inside the string.
        assertThat(tokens).filteredOn(t -> t.type() == KEYWORD)
                .extracting(CodeToken::text).containsExactly("var");
    }

    @Test
    void plainTextLanguageIsLeftUncoloured() {
        List<CodeToken> tokens = highlighter.highlight("if you can read this it is plain", "text");
        assertThat(tokens).singleElement().satisfies(t -> assertThat(t.type()).isEqualTo(PLAIN));
    }
}
