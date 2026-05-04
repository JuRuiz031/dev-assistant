package com.juanruiz.dev_assistant.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitTools {
    private String git(String repoPath, String... args) {

        try {
            List<String> cmd = new ArrayList<>(List.of("git"));
            cmd.addAll(Arrays.asList(args));

            Process p = new ProcessBuilder(cmd)
            .directory(new File(repoPath))
            .redirectErrorStream(true)
            .start();

            boolean finished = p.waitFor(10, TimeUnit.SECONDS);

            if(!finished) {
                p.destroyForcibly();
                return "Error: request timed out";
            }

            String output = new String(p.getInputStream().readAllBytes()).trim();

            if(p.exitValue() != 0) {
                return "git error (exit " + p.exitValue() + "): " + output;
            }


            return output.isEmpty() ? "(no output)" : output;

        } catch (IOException | InterruptedException e) {
            return "Error running git: " + e.getMessage();
        }
    }

    @Tool(description= """
            Returns the last 15 commits of a git repository.
            Shows commit hash, author, relative date, and subject line.
            Use when asked about recent changes, commit history, or who changed something.
            """)
    public String gitLog(
        @ToolParam(description= "Absolute path to the git repo root - the folder that contains .git")
        String repoPath) {
            log.info("[TOOL] gitLog({})", repoPath);
            return git(repoPath, "log", "--oneline", "--graph", "--decorate", "--pretty=format:%h %ar - %s", "-15");
        }

    @Tool(description= """
            Shows the current git status of a repository.
            Lists modified, staged, untracked, and deleted files.
            Use when asked what is currently in progress or what files have changed.
            """)
    public String gitStatus(
        @ToolParam(description= "Absolute path to the git repo root - the folder that contains .git")
    String repoPath) {
        log.info("[TOOL] gitStatus({})", repoPath);
        return git(repoPath, "status");
    }

    @Tool(description= """
            Shows the unified diff of all uncommited changes.
            Returns the actual line-by-line changes across all modified files.
            Use when asked what code was changed or to review current edits.
            """)
    public String gitDiff(
        @ToolParam(description= "Absolute path to the git repo root - the folder that contains .git")
    String repoPath) {
        log.info("[TOOL] gitDiff({})", repoPath);
        String diff = git(repoPath, "diff", "HEAD");
        if(diff.length() > 8000) {
            return diff.substring(0, 8000) + "\n...(diff truncated)";
        }

        return diff;
    }

}