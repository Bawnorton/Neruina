package com.bawnorton.neruina.report;

import com.bawnorton.neruina.platform.Platform;
import com.bawnorton.neruina.util.TickingEntry;
import net.minecraft.SharedConstants;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GitHub;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class IssueFormatter {
    private static final List<Placeholder> PLACEHOLDERS = List.of(
            new Placeholder("date", true, Restriction.NONE, (config, entry) -> DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date())),
            new Placeholder("time", true, Restriction.NONE, (config, entry) -> DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date())),
            new Placeholder("modid", true, Restriction.NONE, (config, entry) -> config.modid()),
            new Placeholder("sources", false, Restriction.NONE, (config, entry) -> {
                Set<String> sources = entry.findPotentialSources();
                return sources.isEmpty() ? "unknown" : String.join(", ", sources);
            }),
            new Placeholder("type", true, Restriction.NONE, (config, entry) -> entry.getCauseType()),
            new Placeholder("name", true, Restriction.NONE, (config, entry) -> entry.getCauseName()),
            new Placeholder("modloader", true, Restriction.NONE, (config, entry) -> Platform.getModLoader().name().toLowerCase(Locale.ROOT)),
            new Placeholder("modversion", false, Restriction.NONE, (config, entry) -> Platform.getModVersion(config.modid())),
            new Placeholder("mcversion", false, Restriction.NONE, (config, entry) -> {
                //? if <=1.21.5 {
                /*return SharedConstants.getCurrentVersion().getName();
                *///?} else {
                return SharedConstants.getCurrentVersion().name();
                //?}
            }),
            new Placeholder("report", false, Restriction.BODY, (config, entry) -> "```\n%s\n```".formatted(entry.createCrashReport()))
    );

    private static final String DEFAULT_TITLE = "[Neruina]: Ticking Exception Auto Report (<date> - <time>)";
    private static final String DEFAULT_BODY = """
        ## Automatic Report Created by **NeruinaAutoReporter**
        Neruina detected a ticking exception in "<modid>" (<type>: <name>)
        
        ### Environment:
        - Minecraft Version: <mcversion>
        - Mod Loader: <modloader>
        - Mod Version: <modversion>
        
        ## Report:
        <details>
        <summary>Generated Crash Report</summary>
        
        <report>
        
        </details>
        
        This issue was created automatically by Neruina's AutoReporter. To opt-out of this feature, remove the `neruina/auto_report.json` file from your mod's data resources.
        """;

    private final AutoReportConfig config;

    private final Map<UUID, GHGist> gists = new HashMap<>();

    public IssueFormatter(AutoReportConfig config) {
        this.config = config;
    }

    private String replacePlaceholders(String input, Restriction restriction, TickingEntry entry) {
        for (Placeholder placeholder : PLACEHOLDERS) {
            if (!placeholder.restriction().allows(restriction)) continue;

            boolean replaced = false;
            String key = "<" + placeholder.key() + ">";
            if (!input.contains(key)) continue;

            String value = placeholder.apply(config, entry);
            for(Placeholder.Modifier modifier : Placeholder.MODIFIERS) {
                if (!modifier.applies(placeholder.key())) continue;

                input = input.replace(modifier.apply(key), value);
                replaced = true;
                break;
            }
            if (!replaced) {
                input = input.replace(key, value);
            }
        }
        return input;
    }

    public String getTitle(TickingEntry entry) {
        String unverifiedTitle = replacePlaceholders((config.title() == null) ? DEFAULT_TITLE : config.title(), Restriction.TITLE, entry);
        if(unverifiedTitle.length() >= 256) {
            return unverifiedTitle.substring(0, 253) + "...";
        } else {
            return unverifiedTitle;
        }
    }

    public String getBody(TickingEntry entry, GitHub github) {
        String rawBody = (config.body() == null) ? DEFAULT_BODY : config.body();
        String unverifiedBody = replacePlaceholders(rawBody, Restriction.BODY, entry);
        if (unverifiedBody.length() < 65536) return unverifiedBody;

        String crashReport = "```\n%s\n```".formatted(entry.createCrashReport());
        GHGist gist = gists.computeIfAbsent(entry.uuid(), uuid -> {
            try {
                return github.createGist()
                        .public_(true)
                        .description("Neruina Auto Report")
                        .file("crash_report.md", crashReport)
                        .create();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create a gist for the crash report", e);
            }
        });
        String gistUrl = gist.getHtmlUrl().toString();
        rawBody = rawBody.replace("<report>", "[Full Crash Report](%s) - Content was too long for an issue body".formatted(gistUrl));
        return replacePlaceholders(rawBody, Restriction.BODY, entry);
    }

    private record Placeholder(String key, boolean isModifiable, Restriction restriction, Applier applier) {
        private static final List<Modifier> MODIFIERS = List.of(
                new Modifier(s -> s.toUpperCase().equals(s), String::toUpperCase),
                new Modifier(Placeholder::isCapitalized, StringUtils::capitalize)
        );

        private static boolean isCapitalized(String s) {
            return Character.isUpperCase(s.charAt(0)) && s.substring(1).equals(s.substring(1).toLowerCase());
        }

        public String apply(AutoReportConfig config, TickingEntry entry) {
            String result = applier.apply(config, entry);
            if(isModifiable) {
                return MODIFIERS.stream()
                        .filter(modifier -> modifier.applies(key))
                        .map(modifier -> modifier.apply(result))
                        .findFirst()
                        .orElse(result);
            }
            return result;
        }

        private record Modifier(Predicate<String> predicate, UnaryOperator<String> modification) {
            public boolean applies(String s) {
                return predicate.test(s);
            }

            public String apply(String s) {
                return modification.apply(s);
            }
        }
    }

    private enum Restriction {
        TITLE, BODY, NONE;

        public boolean allows(Restriction restriction) {
            return switch (this) {
                case NONE -> true;
                case TITLE -> restriction == TITLE;
                case BODY -> restriction == BODY;
            };
        }
    }

    @FunctionalInterface
    private interface Applier {
        String apply(AutoReportConfig config, TickingEntry entry);
    }
}
