package com.bawnorton.neruina.report;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("FieldMayBeFinal") // 1.18.2 uses Gson 2.9 which doesn't support records
public class AutoReportConfig {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String modid;
    private String repo;
    private String title;
    private String body;
    private List<String> labels;
    private List<String> assignees;

    public AutoReportConfig(String modid, String repo, String title, String body, List<String> labels, List<String> assignees) {
        this.modid = modid;
        this.repo = repo;
        this.title = title;
        this.body = body;
        this.labels = labels;
        this.assignees = assignees;
    }

    public boolean isVaild() {
        return modid != null && repo != null;
    }

    public IssueFormatter createIssueFormatter() {
        return new IssueFormatter(this);
    }

    public static AutoReportConfig fromJson(JsonReader reader) {
        return GSON.fromJson(reader, AutoReportConfig.class);
    }

    public String modid() {
        return modid;
    }

    public String repo() {
        return repo;
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }

    public List<String> labels() {
        return labels;
    }

    public List<String> assignees() {
        return assignees;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (AutoReportConfig) obj;
        return Objects.equals(this.modid, that.modid) &&
                Objects.equals(this.repo, that.repo) &&
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.body, that.body) &&
                Objects.equals(this.labels, that.labels) &&
                Objects.equals(this.assignees, that.assignees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modid, repo, title, body, labels, assignees);
    }

    @Override
    public String toString() {
        return "AutoReportConfig[" +
                "modid=" + modid + ", " +
                "repo=" + repo + ", " +
                "title=" + title + ", " +
                "body=" + body + ", " +
                "labels=" + labels + ", " +
                "assignees=" + assignees + ']';
    }
}
