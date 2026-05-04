package com.juanruiz.dev_assistant.tools;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileSystemTools {

    // Folders that are never useful to the LLM — compiled output, git internals, IDE files
    private static final List<String> IGNORED_DIRS = List.of(
        "/target/", "/.git/", "/.mvn/", "/.vscode/", "/.github/", "/.idea/"
    );

    private boolean shouldIgnore(Path p) {
        String pathStr = p.toString();
        return IGNORED_DIRS.stream().anyMatch(pathStr::contains);
    }

    @Tool(description = """
            Lists files and directories at a path, up to 8 levels deep.
            Returns a tree-formatted listing with file sizes in bytes.
            Automatically filters out build output (target/), git internals, and IDE files.
            Use this FIRST when asked about project structure or what files exist.
            Call on the project root to see the full source tree including all Java packages.
            Returns an error message string if the path does not exist.
            """)
    public String listDirectory(
            @ToolParam(description = "Absolute path to list. Example: /home/juanf/ProgrammingProjects/pre_IBM_projects/dev-assistant")
            String path) {

        log.info("[TOOL] listDirectory({})", path);
        try {
            Path dir = Path.of(path);
            if (!Files.exists(dir)) {
                log.warn("[TOOL] listDirectory - path does not exist: {}", path);
                return "Error: path does not exist: " + path;
            }
            if (!Files.isDirectory(dir)) {
                log.warn("[TOOL] listDirectory - not a directory: {}", path);
                return "Error: not a directory: " + path;
            }

            StringBuilder sb = new StringBuilder(dir.toString()).append("\n");
            Files.walk(dir, 8)
                .skip(1)
                .filter(p -> !shouldIgnore(p))
                .forEach(p -> {
                    int depth = dir.relativize(p).getNameCount();
                    String indent = "  ".repeat(depth);
                    String suffix = Files.isDirectory(p) ? "/" : "";
                    String size = "";
                    if (Files.isRegularFile(p)) {
                        try {
                            size = " (" + Files.size(p) + "B)";
                        } catch (IOException ignored) {}
                    }
                    sb.append(indent)
                      .append(p.getFileName())
                      .append(suffix)
                      .append(size)
                      .append("\n");
                });

            String result = sb.toString();
            log.info("[TOOL] listDirectory result:\n{}", result);
            return result;

        } catch (IOException e) {
            log.error("[TOOL] listDirectory error: {}", e.getMessage());
            return "Error listing directory: " + e.getMessage();
        }
    }

    @Tool(description = """
            Reads and returns the full content of a text file.
            Use to read source code, config files, README, pom.xml, YAML, etc.
            For files over ~300 lines, this may be truncated.
            Returns an error string if the file does not exist.
            """)
    public String readFile(
            @ToolParam(description = "Absolute file path. Example: /home/juanf/ProgrammingProjects/pre_IBM_projects/dev-assistant/pom.xml")
            String path) {

        log.info("[TOOL] readFile({})", path);
        try {
            Path file = Path.of(path);
            if (!Files.exists(file)) {
                log.warn("[TOOL] readFile - file not found: {}", path);
                return "Error: file not found: " + path;
            }
            if (!Files.isRegularFile(file)) {
                log.warn("[TOOL] readFile - path is a directory: {}", path);
                return "Error: path is a directory: " + path;
            }

            String content = Files.readString(file);
            if (content.length() > 8000) {
                log.info("[TOOL] readFile result (truncated, first 200 chars): {}",
                    content.substring(0, Math.min(200, content.length())));
                return content.substring(0, 8000)
                    + "\n\n...(truncated at 8000 chars - use readFileRange for specific lines)";
            }

            log.info("[TOOL] readFile result (first 200 chars): {}",
                content.substring(0, Math.min(200, content.length())));
            return content;

        } catch (IOException e) {
            log.error("[TOOL] readFile error: {}", e.getMessage());
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = """
            Finds files matching a glob pattern within a directory.
            Automatically excludes build output and IDE files from results.
            Pattern examples: *.java *Controller* pom.xml *.yml *Service.java
            Returns a list of absolute paths, capped at 50 results.
            Use to locate specific classes or file types across a project.
            """)
    public String findFiles(
            @ToolParam(description = "Root directory to search in")
            String directory,
            @ToolParam(description = "Glob filename pattern. Examples: *.java or *Controller*")
            String pattern) {

        log.info("[TOOL] findFiles({}, {})", directory, pattern);
        try {
            PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:**/" + pattern);

            List<String> found = Files.walk(Path.of(directory))
                .filter(p -> Files.isRegularFile(p)
                    && matcher.matches(p)
                    && !shouldIgnore(p))
                .limit(50)
                .map(Path::toString)
                .sorted()
                .toList();

            if (found.isEmpty()) {
                log.warn("[TOOL] findFiles - no results for pattern '{}' in {}", pattern, directory);
                return "No files found matching '" + pattern + "' in " + directory;
            }

            String result = String.join("\n", found) + "\n\n(" + found.size() + " files found)";
            log.info("[TOOL] findFiles result:\n{}", result);
            return result;

        } catch (IOException e) {
            log.error("[TOOL] findFiles error: {}", e.getMessage());
            return "Error finding files: " + e.getMessage();
        }
    }
}