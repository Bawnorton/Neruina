package com.bawnorton.neruina.report;

import com.bawnorton.neruina.platform.Platform;
import com.bawnorton.neruina.util.TickingEntry;
import net.minecraft.SharedConstants;
import org.apache.commons.lang3.StringUtils;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

public class IssueFormatter {
    private static final List<Placeholder> PLACEHOLDERS = List.of(
            new Placeholder("date", true, Restriction.NONE, (config, entry) -> DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date())),
            new Placeholder("time", true, Restriction.NONE, (config, entry) -> DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date())),
            new Placeholder("modid", true, Restriction.NONE, (config, entry) -> config.modid()),
            new Placeholder("type", true, Restriction.NONE, (config, entry) -> entry.getCauseType()),
            new Placeholder("name", true, Restriction.NONE, (config, entry) -> entry.getCauseName()),
            new Placeholder("modloader", true, Restriction.NONE, (config, entry) -> Platform.getModLoader().name().toLowerCase(Locale.ROOT)),
            new Placeholder("modversion", false, Restriction.NONE, (config, entry) -> Platform.getModVersion(config.modid())),
            new Placeholder("mcversion", false, Restriction.NONE, (config, entry) -> SharedConstants.getGameVersion().getName()),
            new Placeholder("report", false, Restriction.BODY, (config, entry) -> "```\n%s\n```".formatted(entry.createCrashReport().asString()))
    );

    private static final String DEFAULT_TITLE = "[Neruina]: Ticking Exception Auto Report (<date> - <time>)";
    private static final String DEFAULT_BODY = """
        ### Automatic Report Created by **NeruinaAutoReporter**
        Neruina detected a ticking exception in "<modid>" (<type>: <name>)
            
        Generated Report:
        <report>
        """;
    private static final List<String> DEFAULT_LABELS = List.of("bug", "auto-report");

    private final AutoReportConfig config;

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
        return replacePlaceholders((config.title() == null) ? DEFAULT_TITLE : config.title(), Restriction.TITLE, entry);
    }

    public String getBody(TickingEntry entry) {
        return replacePlaceholders((config.body() == null) ? DEFAULT_BODY : config.body(), Restriction.BODY, entry);
    }

    public List<String> getLabels() {
        return config.labels() == null ? DEFAULT_LABELS : config.labels();
    }

    public List<String> getAssignees() {
        return config.assignees() == null ? List.of() : config.assignees();
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

        private record Modifier(Predicate<String> predicate, Function<String, String> modification) {
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
