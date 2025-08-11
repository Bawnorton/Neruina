package com.bawnorton.neruina.report;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.exception.AbortedException;
import com.bawnorton.neruina.exception.InProgressException;
import com.bawnorton.neruina.util.TickingEntry;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class AutoReportHandler {
    private final Set<UUID> reportedEntries = Collections.synchronizedSet(new HashSet<>());
    private final List<AutoReportConfig> configs = new ArrayList<>();
    private final Map<String, RepositoryReference> repositories = new HashMap<>();
    private AutoReportConfig masterConfig;

    public void init(MinecraftServer server) {
        Map<ResourceLocation, Resource> neruinaAutoGhFiles = server.getResourceManager().listResources(Neruina.MOD_ID, (resource) -> resource.getPath().equals("neruina/auto_report.json"));
        for (Map.Entry<ResourceLocation, Resource> entry : neruinaAutoGhFiles.entrySet()) {
            ResourceLocation id = entry.getKey();
            Resource resource = entry.getValue();
            try (JsonReader reader = new JsonReader(resource.openAsReader())) {
                AutoReportConfig config = AutoReportConfig.fromJson(reader);
                if (config.isVaild()) {
                    if (id.getNamespace().equals(Neruina.MOD_ID)) {
                        masterConfig = config;
                        continue;
                    }
                    Neruina.LOGGER.info("Auto report config loaded for mod: \"{}\"", config.modid());
                    configs.add(config);
                } else {
                    Neruina.LOGGER.warn("Invalid auto report config found: {}, ignoring", id);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (masterConfig == null) {
            Neruina.LOGGER.warn("No master auto report config found, creating default");
            masterConfig = new AutoReportConfig("*", "Bawnorton/NeruinaAutoReports", null, null);
        }
    }

    public CompletableFuture<ReportStatus> createReports(ServerPlayer player, TickingEntry entry) {
        return createReports(player, entry, true);
    }

    public CompletableFuture<ReportStatus> createReports(ServerPlayer player, TickingEntry entry, boolean publish) {
        UUID entryId = entry.uuid();
        if (reportedEntries.contains(entryId)) {
            return CompletableFuture.completedFuture(ReportStatus.alreadyExists());
        }

        Either<GitHub, ReportStatus> result = GithubAuthManager.getOrLogin(player)
                                                               .thenApply(Either::<GitHub, ReportStatus>left)
                                                               .exceptionally(throwable -> {
                                                                   Throwable cause = throwable.getCause();
                                                                   if (cause instanceof InProgressException) {
                                                                       return Either.right(ReportStatus.inProgress());
                                                                   } else if (cause instanceof CancellationException) {
                                                                       return Either.right(ReportStatus.timeout());
                                                                   } else if (cause instanceof AbortedException) {
                                                                       return Either.right(ReportStatus.aborted());
                                                                   }
                                                                   Neruina.LOGGER.error("Failed to create report(s)", throwable);
                                                                   return Either.right(ReportStatus.failure());
                                                               }).join();

        if (result.right().isPresent()) {
            return CompletableFuture.completedFuture(result.right().orElseThrow());
        }

        GitHub github = result.left().orElseThrow();

        reportedEntries.add(entryId);
        Set<String> modids = entry.findPotentialSources();
        Map<String, GHIssueBuilder> issueBuilders = new HashMap<>();
        for (String modid : modids) {
            issueBuilders.put(modid, null);
            RepositoryReference repository = repositories.computeIfAbsent(modid, key -> {
                for (AutoReportConfig config : configs) {
                    String listeningModid = config.modid();
                    if (!listeningModid.equals("*") && !listeningModid.equals(modid)) continue;
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
            if (repository == null) continue;

            try {
                GHIssueBuilder issue = createIssue(github, repository, entry);
                issueBuilders.put(modid, issue);
            } catch (RuntimeException e) {
                Neruina.LOGGER.error("Failed to create issue for mod: \"{}\"", modid, e);
                return CompletableFuture.completedFuture(ReportStatus.failure());
            }
        }

        Map<String, GHIssue> builtIssues = new HashMap<>();
        if (publish) {
            issueBuilders.forEach((s, ghIssueBuilder) -> {
                if (ghIssueBuilder == null) return;
                try {
                    builtIssues.put(s, ghIssueBuilder.create());
                } catch (IOException e) {
                    Neruina.LOGGER.error("Failed to create issue for mod: \"{}\"", s, e);
                }
            });
        }

        GHIssueBuilder masterIssueBuilder;

        try {
            masterIssueBuilder = createMasterIssue(github, builtIssues, entry);
        } catch (RuntimeException e) {
            Neruina.LOGGER.error("Failed to create master issue", e);
            return CompletableFuture.completedFuture(ReportStatus.failure());
        }
        if (masterIssueBuilder == null) return CompletableFuture.completedFuture(ReportStatus.failure());

        try {
            if (publish) {
                GHIssue masterIssue = masterIssueBuilder.create();
                String url = masterIssue.getHtmlUrl().toString();
                Neruina.LOGGER.info(
                        "Report(s) created for ticking entry: ({}: {})",
                        entry.getCauseType(),
                        entry.getCauseName()
                );
                return CompletableFuture.completedFuture(ReportStatus.success(url));
            } else {
                return CompletableFuture.completedFuture(ReportStatus.testing());
            }
        } catch (IOException e) {
            Neruina.LOGGER.error("Failed to create master issue", e);
            return CompletableFuture.completedFuture(ReportStatus.failure());
        }
    }

    private GHIssueBuilder createMasterIssue(GitHub github, Map<String, GHIssue> issueMap, TickingEntry tickingEntry) {
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

        IssueFormatter formatter = masterConfig.createIssueFormatter();
        String body = "%s".formatted(formatter.getBody(tickingEntry, github));
        if (!issueMap.isEmpty()) {
            body = """
                    ## Associated Issues:
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
        return masterRepo.createIssueBuilder(formatter.getTitle(tickingEntry)).body(body);
    }

    private GHIssueBuilder createIssue(GitHub github, RepositoryReference reference, TickingEntry entry) {
        AutoReportConfig config = reference.config();
        IssueFormatter formatter = config.createIssueFormatter();
        return reference.createIssueBuilder(formatter.getTitle(entry))
                        .body(formatter.getBody(entry, github));
    }

    public void testReporting(ServerPlayer player) {
        TickingEntry dummyEntry = new TickingEntry(
                player,
                false,
                player.level().dimension(),
                player.getOnPos(),
                new RuntimeException()
        );
        createReports(player, dummyEntry, false);
    }
}
