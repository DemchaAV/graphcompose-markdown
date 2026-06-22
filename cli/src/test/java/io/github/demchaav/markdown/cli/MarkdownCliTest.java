package io.github.demchaav.markdown.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives the CLI in-process through {@link MarkdownCli#newCommandLine()} — the same wiring
 * {@code main} uses — and asserts the exit-code contract and that errors degrade to a clean
 * {@code error: …} message rather than a raw stack trace.
 */
class MarkdownCliTest {

    @TempDir
    Path tmp;

    private record CliResult(int exitCode, String err) {
    }

    /** Runs the CLI, capturing whatever it writes to {@code System.err}. */
    private CliResult run(String... args) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            int code = MarkdownCli.newCommandLine().execute(args);
            return new CliResult(code, buffer.toString(StandardCharsets.UTF_8));
        } finally {
            System.setErr(originalErr);
        }
    }

    private Path writeMarkdown(String name, String content) throws IOException {
        Path file = tmp.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    @Test
    void rendersMarkdownToPdf() throws IOException {
        Path input = writeMarkdown("in.md", "# Hello\n\nWorld with a [link](https://example.com).\n");
        Path output = tmp.resolve("out.pdf");

        CliResult result = run(input.toString(), "-o", output.toString());

        assertThat(result.exitCode()).isZero();
        assertThat(Files.exists(output)).isTrue();
        byte[] head = Files.readAllBytes(output);
        assertThat(new String(head, 0, Math.min(5, head.length), StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void missingInputFileExitsWithCodeTwo() {
        CliResult result = run("definitely-does-not-exist.md");

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.err()).contains("input file not found");
    }

    @Test
    void unknownThemeExitsWithCodeTwo() throws IOException {
        Path input = writeMarkdown("in.md", "# Title\n");

        CliResult result = run(input.toString(), "-t", "no-such-theme");

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.err()).contains("unknown theme");
    }

    @Test
    void versionFlagExitsZero() {
        CliResult result = run("--version");

        assertThat(result.exitCode()).isZero();
    }

    @Test
    void nonUtf8InputFailsCleanlyWithCodeTwo() throws IOException {
        // A UTF-16/BOM (or ANSI) file: 0xFF 0xFE is not valid UTF-8, so the strict decoder
        // throws. The CLI must report a clean error, not leak a MalformedInputException trace.
        Path input = tmp.resolve("bad.md");
        Files.write(input, new byte[] {(byte) 0xFF, (byte) 0xFE, '#', ' ', 'x'});

        CliResult result = run(input.toString());

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.err()).contains("as UTF-8");
        assertThat(result.err()).doesNotContain("\tat "); // no stack frame leaked
    }

    @Test
    void unwritableOutputDirectoryFailsCleanly() throws IOException {
        Path input = writeMarkdown("in.md", "# Title\n");
        // A regular file standing where a parent directory would need to be: createDirectories
        // throws, and the execution-exception handler must catch it cleanly (no stack trace).
        Path blocker = tmp.resolve("blocker");
        Files.writeString(blocker, "not a directory");
        Path output = blocker.resolve("sub").resolve("out.pdf");

        CliResult result = run(input.toString(), "-o", output.toString());

        assertThat(result.exitCode()).isNotZero();
        assertThat(result.err()).startsWith("error:");
        assertThat(result.err()).doesNotContain("\tat "); // no stack frame leaked
    }
}
