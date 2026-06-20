package io.github.demchaav.markdown.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The built-in {@link SyntaxHighlighter}: a dependency-free, regex-based tokenizer
 * covering common languages (keywords, strings, comments, numbers, annotations,
 * call names).
 *
 * <p>It scans a single combined pattern per language so a keyword inside a string
 * or comment is not mis-coloured. It is deliberately "good enough" for documentation
 * — it does not parse grammar. Unknown but non-empty languages get a conservative
 * generic pass (strings + numbers); an empty or {@code text}/{@code plaintext}
 * language is left {@link CodeTokenType#PLAIN}. For full fidelity, plug a
 * grammar-based {@link SyntaxHighlighter} into the theme instead.</p>
 */
public final class RegexSyntaxHighlighter implements SyntaxHighlighter {

    // Declared before SPECS so they are initialised when buildSpecs() runs (static order).
    private static final List<String[]> SLASH_BLOCK = List.<String[]>of(new String[]{"/*", "*/"});
    private static final List<String> SLASH_LINE = List.of("//");
    private static final List<String> HASH_LINE = List.of("#");
    private static final List<String[]> NO_BLOCK = List.of();
    private static final List<String> NO_LINE = List.of();

    private static final Map<String, Spec> SPECS = buildSpecs();
    private final Map<String, Optional<Pattern>> cache = new ConcurrentHashMap<>();

    @Override
    public List<CodeToken> highlight(String code, String language) {
        if (code == null || code.isEmpty()) {
            return List.of();
        }
        Optional<Pattern> pattern = patternFor(language);
        if (pattern.isEmpty()) {
            return List.of(new CodeToken(code, CodeTokenType.PLAIN));
        }
        return tokenize(code, pattern.get());
    }

    private Optional<Pattern> patternFor(String language) {
        String key = canonical(language);
        return cache.computeIfAbsent(key, k -> {
            Spec spec = SPECS.get(k);
            return spec == null ? Optional.empty() : Optional.of(compile(spec));
        });
    }

    private static List<CodeToken> tokenize(String code, Pattern pattern) {
        List<CodeToken> tokens = new ArrayList<>();
        Matcher matcher = pattern.matcher(code);
        int last = 0;
        while (matcher.find()) {
            if (matcher.start() == matcher.end()) {
                continue; // never advance on an empty match
            }
            if (matcher.start() > last) {
                tokens.add(new CodeToken(code.substring(last, matcher.start()), CodeTokenType.PLAIN));
            }
            tokens.add(new CodeToken(matcher.group(), typeOf(matcher)));
            last = matcher.end();
        }
        if (last < code.length()) {
            tokens.add(new CodeToken(code.substring(last), CodeTokenType.PLAIN));
        }
        return tokens;
    }

    private static CodeTokenType typeOf(Matcher matcher) {
        if (group(matcher, "comment") != null) {
            return CodeTokenType.COMMENT;
        }
        if (group(matcher, "string") != null) {
            return CodeTokenType.STRING;
        }
        if (group(matcher, "anno") != null) {
            return CodeTokenType.ANNOTATION;
        }
        if (group(matcher, "number") != null) {
            return CodeTokenType.NUMBER;
        }
        if (group(matcher, "keyword") != null) {
            return CodeTokenType.KEYWORD;
        }
        if (group(matcher, "func") != null) {
            return CodeTokenType.FUNCTION;
        }
        return CodeTokenType.PLAIN;
    }

    private static String group(Matcher matcher, String name) {
        try {
            return matcher.group(name);
        } catch (IllegalArgumentException noSuchGroup) {
            return null; // the pattern for this language omits that group
        }
    }

    private static Pattern compile(Spec spec) {
        List<String> alts = new ArrayList<>();

        List<String> comments = new ArrayList<>();
        for (String[] block : spec.blockComments) {
            comments.add(Pattern.quote(block[0]) + "[\\s\\S]*?" + Pattern.quote(block[1]));
        }
        for (String line : spec.lineComments) {
            comments.add(Pattern.quote(line) + "[^\\n]*");
        }
        if (!comments.isEmpty()) {
            alts.add("(?<comment>" + String.join("|", comments) + ")");
        }

        List<String> strings = new ArrayList<>();
        for (char q : spec.strings.toCharArray()) {
            strings.add(stringPart(q, false));
        }
        for (char q : spec.multilineStrings.toCharArray()) {
            strings.add(stringPart(q, true));
        }
        if (!strings.isEmpty()) {
            alts.add("(?<string>" + String.join("|", strings) + ")");
        }

        if (spec.annotations) {
            alts.add("(?<anno>@[A-Za-z_][A-Za-z0-9_.]*)");
        }

        alts.add("(?<number>\\b0[xX][0-9a-fA-F]+\\b|\\b\\d[\\d_]*(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b)");

        if (!spec.keywords.isEmpty()) {
            alts.add("(?<keyword>\\b(?:" + String.join("|", spec.keywords) + ")\\b)");
        }

        if (spec.functions) {
            alts.add("(?<func>[A-Za-z_][A-Za-z0-9_]*(?=\\s*\\())");
        }

        int flags = spec.caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
        return Pattern.compile(String.join("|", alts), flags);
    }

    /** A string literal: opening quote, escaped chars or non-quote chars, closing quote. */
    private static String stringPart(char q, boolean multiline) {
        String s = String.valueOf(q);
        String newline = multiline ? "" : "\\n";
        return s + "(?:\\\\.|[^" + s + "\\\\" + newline + "])*" + s;
    }

    private static String canonical(String language) {
        if (language == null) {
            return "";
        }
        String lang = language.trim().toLowerCase(java.util.Locale.ROOT);
        switch (lang) {
            case "":
            case "text":
            case "plaintext":
            case "plain":
            case "none":
            case "txt":
                return "";
            case "js":
            case "jsx":
            case "mjs":
            case "node":
                return "javascript";
            case "ts":
            case "tsx":
                return "typescript";
            case "py":
            case "py3":
            case "python3":
                return "python";
            case "kt":
            case "kts":
                return "kotlin";
            case "sh":
            case "bash":
            case "zsh":
            case "shell-session":
                return "shell";
            case "c++":
            case "cc":
            case "hpp":
                return "cpp";
            case "h":
                return "c";
            case "cs":
                return "csharp";
            case "yml":
                return "yaml";
            case "html":
            case "htm":
            case "svg":
                return "xml";
            case "rs":
                return "rust";
            default:
                return SPECS.containsKey(lang) ? lang : "generic";
        }
    }

    /** A language tokenization spec. {@code strings}/{@code multilineStrings} are quote characters. */
    private record Spec(List<String> keywords, List<String[]> blockComments, List<String> lineComments,
                        String strings, String multilineStrings, boolean annotations, boolean functions,
                        boolean caseInsensitive) {
    }

    private static List<String> words(String space) {
        return List.of(space.trim().split("\\s+"));
    }

    private static Map<String, Spec> buildSpecs() {
        Map<String, Spec> m = new java.util.HashMap<>();

        m.put("java", new Spec(words(
                "abstract assert boolean break byte case catch char class const continue default do double else "
                        + "enum extends final finally float for goto if implements import instanceof int interface long "
                        + "native new package private protected public return short static strictfp super switch "
                        + "synchronized this throw throws transient try void volatile while var record sealed permits "
                        + "yield true false null"),
                SLASH_BLOCK, SLASH_LINE, "\"'", "", true, true, false));

        m.put("kotlin", new Spec(words(
                "as break by class companion const continue crossinline data do dynamic else enum external false final "
                        + "finally for fun if import in init inner interface internal is lateinit native object open "
                        + "operator out override package private protected public reified return sealed super suspend "
                        + "this throw true try typealias val var vararg when where while null abstract annotation"),
                SLASH_BLOCK, SLASH_LINE, "\"'", "", true, true, false));

        Spec js = new Spec(words(
                "async await break case catch class const continue debugger default delete do else export extends "
                        + "finally for from function get if import in instanceof let new of return set static super "
                        + "switch this throw try typeof var void while with yield null true false undefined "
                        + "interface type enum implements public private protected readonly as keyof namespace declare"),
                SLASH_BLOCK, SLASH_LINE, "\"'", "`", true, true, false);
        m.put("javascript", js);
        m.put("typescript", js);

        m.put("python", new Spec(words(
                "and as assert async await break class continue def del elif else except finally for from global if "
                        + "import in is lambda nonlocal not or pass raise return try while with yield None True False "
                        + "match case self"),
                NO_BLOCK, HASH_LINE, "\"'", "", true, true, false));

        m.put("sql", new Spec(words(
                "select from where insert into update delete create table drop alter add column join inner left right "
                        + "outer full on group by order having union all as distinct values set and or not null is in "
                        + "like between case when then else end limit offset primary key foreign references default "
                        + "index view database schema grant revoke begin commit rollback transaction asc desc count "
                        + "sum avg min max exists"),
                SLASH_BLOCK, List.of("--"), "'\"", "", false, false, true));

        m.put("shell", new Spec(words(
                "if then else elif fi for while until do done case esac function in select return break continue "
                        + "export local readonly declare unset echo printf cd pushd popd source eval exec set"),
                NO_BLOCK, HASH_LINE, "\"'", "", false, false, false));

        m.put("go", new Spec(words(
                "break case chan const continue default defer else fallthrough for func go goto if import interface "
                        + "map package range return select struct switch type var nil true false iota"),
                SLASH_BLOCK, SLASH_LINE, "\"'", "`", false, true, false));

        m.put("rust", new Spec(words(
                "as async await break const continue crate dyn else enum extern false fn for if impl in let loop match "
                        + "mod move mut pub ref return self Self static struct super trait true type unsafe use where "
                        + "while box"),
                // Only double-quoted strings: a single quote in Rust is usually a lifetime
                // ('a), so treating ' as a string delimiter would swallow the rest of the line.
                SLASH_BLOCK, SLASH_LINE, "\"", "", false, true, false));

        Spec cLike = new Spec(words(
                "auto bool break case char class const constexpr continue default delete do double else enum extern "
                        + "false float for friend goto if inline int long namespace new nullptr operator private protected "
                        + "public register return short signed sizeof static struct switch template this throw true try "
                        + "typedef typename union unsigned using virtual void volatile while string vector"),
                SLASH_BLOCK, SLASH_LINE, "\"'", "", false, true, false);
        m.put("c", cLike);
        m.put("cpp", cLike);

        m.put("csharp", new Spec(words(
                "abstract as base bool break byte case catch char checked class const continue decimal default delegate "
                        + "do double else enum event explicit extern false finally fixed float for foreach goto if "
                        + "implicit in int interface internal is lock long namespace new null object operator out "
                        + "override params private protected public readonly ref return sealed short sizeof static string "
                        + "struct switch this throw true try typeof uint ulong unchecked unsafe ushort using var virtual "
                        + "void volatile while async await yield"),
                SLASH_BLOCK, SLASH_LINE, "\"'", "", false, true, false));

        m.put("json", new Spec(words("true false null"),
                NO_BLOCK, NO_LINE, "\"", "", false, false, false));

        m.put("yaml", new Spec(words("true false null yes no on off"),
                NO_BLOCK, HASH_LINE, "\"'", "", false, false, false));

        m.put("xml", new Spec(List.of(),
                List.<String[]>of(new String[]{"<!--", "-->"}), NO_LINE, "\"'", "", false, false, false));

        m.put("css", new Spec(List.of(),
                SLASH_BLOCK, NO_LINE, "\"'", "", false, false, false));

        m.put("generic", new Spec(List.of(),
                NO_BLOCK, NO_LINE, "\"'", "`", false, false, false));

        return Map.copyOf(m);
    }
}
