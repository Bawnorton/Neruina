package com.bawnorton.neruina.report;

import com.bawnorton.neruina.util.TickingEntry;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class IssueFormatter {
    private static final String DEFAULT_TITLE = "[Neruina]: Ticking Exception Auto Report (<type>: <name>)";
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

    public String getTitle(TickingEntry entry) {
        return ((config.title() == null) ? DEFAULT_TITLE : config.title())
                .replace("<date>", DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()))
                .replace("<time>", DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()))
                .replace("<modid>", config.modid())
                .replace("<type>", entry.getCauseType())
                .replace("<name>", entry.getCauseName());
    }

    public String getBody(TickingEntry entry) {
        return ((config.body() == null) ? DEFAULT_BODY : config.body())
                .replace("<date>", DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()))
                .replace("<time>", DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()))
                .replace("<modid>", config.modid())
                .replace("<type>", entry.getCauseType())
                .replace("<name>", entry.getCauseName())
                .replace("<report>", "```\n%s\n```".formatted(entry.createCrashReport().asString()));
    }

    public List<String> getLabels() {
        return config.labels() == null ? DEFAULT_LABELS : config.labels();
    }

    public List<String> getAssignees() {
        return config.assignees() == null ? List.of() : config.assignees();
    }
}
