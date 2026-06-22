package io.github.demchaav.markdown.cli;

import io.github.demchaav.markdown.composer.MarkdownComposer;
import io.github.demchaav.markdown.extension.BundledFonts;
import io.github.demchaav.markdown.extension.DefaultImageResolver;
import io.github.demchaav.markdown.extension.EmojiResolver;
import io.github.demchaav.markdown.theme.DefaultMarkdownTheme;
import io.github.demchaav.markdown.theme.MarkdownTheme;
import io.github.demchaav.markdown.theme.packs.AcademicTheme;
import io.github.demchaav.markdown.theme.packs.BusinessReportTheme;
import io.github.demchaav.markdown.theme.packs.GitHubTheme;
import io.github.demchaav.markdown.theme.packs.MinimalTheme;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Command-line front end: render a Markdown file (or stdin) to a themed PDF. A thin
 * wrapper over {@link MarkdownComposer} — all rendering is the library; this class only
 * parses arguments, wires the chosen theme / resolvers, and writes the output.
 */
@Command(
        name = "gcmd",
        mixinStandardHelpOptions = true,
        version = "graph-compose-markdown CLI 0.2.0-SNAPSHOT",
        sortOptions = false,
        description = "Render a Markdown file to a themed PDF (powered by graph-compose-markdown).")
public final class MarkdownCli implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "INPUT",
            description = "Markdown file to render, or '-' to read from standard input.")
    private String input;

    @Option(names = {"-o", "--output"}, paramLabel = "FILE",
            description = "Output PDF path, or '-' to write the PDF to standard output. "
                    + "Default: the input name with a .pdf extension (out.pdf for stdin).")
    private Path output;

    @Option(names = {"-t", "--theme"}, paramLabel = "NAME", defaultValue = "light",
            description = "Theme: light, dark, github-light, github-dark, academic, minimal, business. Default: light.")
    private String themeName;

    @Option(names = {"-i", "--images"}, paramLabel = "DIR",
            description = "Base directory for relative image paths. Default: the input file's directory.")
    private Path imagesDir;

    @Option(names = {"-e", "--emoji"}, paramLabel = "DIR",
            description = "Directory of emoji PNGs named <shortcode>.png; enables :shortcode: inline images.")
    private Path emojiDir;

    @Option(names = {"--mono-jetbrains"},
            description = "Render code in the bundled JetBrains Mono font (bundled in this CLI).")
    private boolean monoJetbrains;

    @Option(names = {"--strict"},
            description = "Fail on unsupported Markdown instead of degrading gracefully.")
    private boolean strict;

    @Override
    public Integer call() throws Exception {
        boolean stdin = "-".equals(input);
        Path inputPath = stdin ? null : Path.of(input);
        if (!stdin && !Files.isRegularFile(inputPath)) {
            System.err.println("error: input file not found: " + input);
            return 2;
        }

        MarkdownTheme base = resolveTheme(themeName);
        if (base == null) {
            System.err.println("error: unknown theme '" + themeName + "'. Valid: "
                    + "light, dark, github-light, github-dark, academic, minimal, business.");
            return 2;
        }

        String markdown;
        try {
            markdown = stdin
                    ? new String(System.in.readAllBytes(), StandardCharsets.UTF_8)
                    : Files.readString(inputPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // A file that is not valid UTF-8 (a Windows ANSI / UTF-16 / BOM file, say) makes
            // the strict decoder throw MalformedInputException; report it cleanly as an input
            // error (exit 2) instead of leaking the decoder's stack trace.
            System.err.println("error: cannot read " + (stdin ? "<stdin>" : input) + " as UTF-8: "
                    + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            return 2;
        }

        boolean toStdout = output != null && "-".equals(output.toString());
        Path imgBase = imagesDir != null ? imagesDir
                : (inputPath != null && inputPath.toAbsolutePath().getParent() != null
                        ? inputPath.toAbsolutePath().getParent() : Path.of("."));

        var themeBuilder = MarkdownTheme.builder(base).imageResolver(new DefaultImageResolver(imgBase));
        if (emojiDir != null) {
            themeBuilder.emojiResolver(fileEmojiResolver(emojiDir));
        }
        MarkdownTheme theme = themeBuilder.build();
        if (monoJetbrains) {
            theme = BundledFonts.jetBrainsMonoCode(theme);
        }

        MarkdownComposer composer = MarkdownComposer.builder().theme(theme).strictMode(strict).build();

        // The status line always goes to stderr, so stdout can carry the raw PDF bytes for piping.
        if (toStdout) {
            try {
                composer.render(markdown).writePdf(System.out);
                System.out.flush();
            } catch (Exception e) {
                System.err.println("error: failed to render: "
                        + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                return 1;
            }
            System.err.println("Rendered " + (stdin ? "<stdin>" : input) + " -> <stdout>");
            return 0;
        }

        Path out = output != null ? output : defaultOutput(stdin, inputPath);
        Path parent = out.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try {
            composer.render(markdown).writePdf(out);
        } catch (Exception e) {
            System.err.println("error: failed to render: "
                    + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            return 1;
        }

        System.err.println("Rendered " + (stdin ? "<stdin>" : input) + " -> " + out
                + " (" + Files.size(out) + " bytes)");
        return 0;
    }

    private static Path defaultOutput(boolean stdin, Path inputPath) {
        if (stdin) {
            return Path.of("out.pdf");
        }
        String name = inputPath.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name;
        Path parent = inputPath.toAbsolutePath().getParent();
        return parent != null ? parent.resolve(stem + ".pdf") : Path.of(stem + ".pdf");
    }

    private static MarkdownTheme resolveTheme(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "light":
                return DefaultMarkdownTheme.light();
            case "dark":
                return DefaultMarkdownTheme.dark();
            case "github":
            case "github-light":
                return GitHubTheme.light();
            case "github-dark":
                return GitHubTheme.dark();
            case "academic":
                return AcademicTheme.light();
            case "minimal":
                return MinimalTheme.light();
            case "business":
            case "business-report":
                return BusinessReportTheme.light();
            default:
                return null;
        }
    }

    /** Resolves {@code :shortcode:} to {@code <dir>/<shortcode>.png} on the filesystem. */
    private static EmojiResolver fileEmojiResolver(Path dir) {
        return shortcode -> {
            Path png = dir.resolve(shortcode + ".png");
            try {
                return Files.isRegularFile(png) ? Optional.of(Files.readAllBytes(png)) : Optional.empty();
            } catch (IOException e) {
                return Optional.empty();
            }
        };
    }

    /**
     * Builds the configured {@link CommandLine}. Package-private so tests can drive
     * {@code newCommandLine().execute(args)} through the same wiring {@link #main} uses.
     *
     * @return a command line with the clean-error execution handler installed
     */
    static CommandLine newCommandLine() {
        CommandLine cmd = new CommandLine(new MarkdownCli());
        // Turn any uncaught exception (an unwritable output directory, a missing bundled font,
        // …) into a clean `error: <message>` on stderr with a non-zero exit, instead of letting
        // picocli's default handler dump a raw Java stack trace at the user.
        cmd.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
            String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            commandLine.getErr().println("error: " + message);
            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        });
        return cmd;
    }

    public static void main(String[] args) {
        System.exit(newCommandLine().execute(args));
    }
}
