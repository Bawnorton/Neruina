package com.bawnorton.neruina.report;

import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;

public record RepositoryReference(String modid, GHRepository githubRepo, AutoReportConfig config) {
    public GHIssueBuilder createIssueBuilder(String title) {
        return githubRepo.createIssue(title);
    }
}
