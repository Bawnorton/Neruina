package com.bawnorton.neruina.report;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.platform.Platform;
import com.bawnorton.neruina.util.Reflection;
import com.bawnorton.neruina.util.TickingEntry;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resource.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class AutoReportHandler {
    private final Set<TickingEntry> reportedEntries = new HashSet<>();
    private final List<AutoReportConfig> configs = new ArrayList<>();
    private final Map<String, RepositoryReference> repositories = new HashMap<>();
    private AutoReportConfig masterConfig;

    public void init(MinecraftServer server) {
        /*? if >=1.19 { */
        Map<Identifier, Resource> neruinaAutoGhFiles = server.getResourceManager().findResources(Neruina.MOD_ID, (resource) -> resource.getPath().equals("neruina/auto_report.json"));
        for (Map.Entry<Identifier, Resource> entry : neruinaAutoGhFiles.entrySet()) {
            Identifier id = entry.getKey();
            Resource resource = entry.getValue();
            try (JsonReader reader = new JsonReader(resource.getReader())) {
                AutoReportConfig config = AutoReportConfig.fromJson(reader);
                if (config.isVaild()) {
                    if (config.modid().equals(Neruina.MOD_ID)) {
                        masterConfig = config;
                        continue;
                    }
                    configs.add(config);
                } else {
                    Neruina.LOGGER.warn("Invalid auto report config found: {}", id);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        /*? } else { *//*
        Collection<Identifier> neruinaAutoGhFiles = server.getResourceManager().findResources(Neruina.MOD_ID, path -> path.equals("auto_report.json"));
        for (Identifier id : neruinaAutoGhFiles) {
            try (Resource resource = server.getResourceManager().getResource(id)) {
                InputStream io = resource.getInputStream();
                JsonReader reader = new JsonReader(new InputStreamReader(io));
                AutoReportConfig config = AutoReportConfig.fromJson(reader);
                if (config.isVaild()) {
                    if (config.modid().equals(Neruina.MOD_ID)) {
                        masterConfig = config;
                        continue;
                    }
                    configs.add(config);
                } else {
                    Neruina.LOGGER.warn("Invalid auto report config found: {}", id);
                }
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        *//*? }*/
    }

    public CompletableFuture<Pair<ReportCode, String>> createReports(TickingEntry entry) {
        return GithubAuthManager.getOrLogin().thenApply(github -> {
            if (reportedEntries.contains(entry)) {
                return Pair.of(ReportCode.ALREADY_EXISTS, "");
            }

            reportedEntries.add(entry);
            Set<String> modids = findPotentialSources(entry);
            Map<String, @Nullable GHIssue> issues = new HashMap<>();
            modids.forEach(modid -> {
                issues.put(modid, null);
                RepositoryReference repository = repositories.computeIfAbsent(modid, key -> {
                    for (AutoReportConfig config : configs) {
                        if (!config.modid().equals(modid)) continue;

                        try {
                            GHRepository ghRepository = github.getRepository(config.repo());
                            return new RepositoryReference(modid, ghRepository, config);
                        } catch (IOException e) {
                            Neruina.LOGGER.error(
                                    "Failed to get repository for mod: \"{}\", report this to them.",
                                    modid,
                                    e
                            );
                        }
                    }
                    return null;
                });
                if (repository == null) return;

                GHIssue issue = createIssue(repository, entry);
                if (issue != null) {
                    issues.put(modid, issue);
                }
            });
            GHIssue masterIssue = createMasterIssue(github, issues, entry);
            String url = masterIssue != null ? masterIssue.getHtmlUrl().toString() : null;
            return Pair.of(ReportCode.SUCCESS, url);
        }).thenApply(result -> {
            Neruina.LOGGER.info(
                    "Report(s) created for ticking entry: ({}: {})",
                    entry.getCauseType(),
                    entry.getCauseName()
            );
            return result;
        }).exceptionally(throwable -> {
            Neruina.LOGGER.error("Failed to create report(s)", throwable);
            return Pair.of(ReportCode.FAILURE, "");
        });
    }

    private GHIssue createMasterIssue(GitHub github, Map<String, GHIssue> issueMap, TickingEntry tickingEntry) {
        if (masterConfig == null) return null;

        RepositoryReference masterRepo = repositories.computeIfAbsent(Neruina.MOD_ID, key -> {
            try {
                GHRepository ghRepository = github.getRepository(masterConfig.repo());
                return new RepositoryReference(Neruina.MOD_ID, ghRepository, masterConfig);
            } catch (IOException e) {
                return null;
            }
        });
        if (masterRepo == null) return null;

        String body = "%s".formatted(masterConfig.createIssueFormatter().getBody(tickingEntry));
        if (!issueMap.isEmpty()) {
            body = """
                    Associated Issues:
                    %s
                            
                    %s
                    """.formatted(
                    issueMap.entrySet()
                            .stream()
                            .map(entry -> {
                                String modid = entry.getKey();
                                GHIssue issue = entry.getValue();
                                if (issue == null) {
                                    return "- %s: Not opted into auto-reporting".formatted(modid);
                                }
                                return "- [%s](%s)".formatted(modid, issue.getHtmlUrl().toString());
                            })
                            .collect(Collectors.joining("\n")),
                    body
            );
        }
        IssueFormatter formatter = masterConfig.createIssueFormatter();
        GHIssueBuilder builder = masterRepo.createIssueBuilder(formatter.getTitle(tickingEntry)).body(body);
        formatter.getLabels().forEach(builder::label);
        formatter.getAssignees().forEach(builder::assignee);
        issueMap.keySet().forEach(builder::label);
        try {
            return builder.create();
        } catch (IOException e) {
            Neruina.LOGGER.error("Failed to create master issue", e);
            return null;
        }
    }

    private GHIssue createIssue(RepositoryReference reference, TickingEntry entry) {
        AutoReportConfig config = reference.config();
        IssueFormatter formatter = config.createIssueFormatter();
        GHIssueBuilder builder = reference.createIssueBuilder(formatter.getTitle(entry))
                .body(formatter.getBody(entry));
        formatter.getLabels().forEach(builder::label);
        formatter.getAssignees().forEach(builder::assignee);
        try {
            return builder.create();
        } catch (IOException e) {
            Neruina.LOGGER.error("Failed to create issue for mod: \"{}\", report this to them.", reference.modid(), e);
            return null;
        }
    }

    private Set<String> findPotentialSources(TickingEntry entry) {
        Throwable exception = entry.error();
        StackTraceElement[] stackTrace = exception.getStackTrace();
        Set<String> modids = new HashSet<>();
        for (StackTraceElement element : stackTrace) {
            Class<?> clazz;
            try {
                clazz = Class.forName(element.getClassName());
            } catch (ClassNotFoundException ignored) {
                continue;
            }

            String methodName = element.getMethodName();
            String modid = checkForMixin(clazz, methodName);
            if (modid != null && !modid.equals("minecraft")) {
                modids.add(modid);
                continue;
            }

            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource == null) continue;

            String location = codeSource.getLocation().getPath();
            location = location.substring(location.lastIndexOf('/') + 1);
            modid = Platform.modidFromJar(location);
            if (modid != null && !modid.equals("minecraft")) {
                modids.add(modid);
            }
        }
        return modids;
    }

    private @Nullable String checkForMixin(Class<?> clazz, String methodName) {
        MixinMerged annotation;
        Method method = Reflection.findMethod(clazz, methodName);
        if (method == null) return null;

        if (!method.isAnnotationPresent(MixinMerged.class)) return null;

        annotation = method.getAnnotation(MixinMerged.class);
        String mixinClassName = annotation.mixin();
        ClassLoader classLoader = clazz.getClassLoader();
        URL resource = classLoader.getResource(mixinClassName.replace('.', '/') + ".class");
        if (resource == null) return null;

        String location = resource.getPath();
        location = location.substring(location.lastIndexOf('/') + 1);
        return Platform.modidFromJar(location);
    }
}
