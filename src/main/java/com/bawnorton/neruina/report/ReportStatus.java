package com.bawnorton.neruina.report;

import org.jetbrains.annotations.Nullable;

public record ReportStatus(Code code, @Nullable String message) {
	private ReportStatus(Code code) {
		this(code, null);
	}

	public static ReportStatus alreadyExists() {
		return new ReportStatus(Code.ALREADY_EXISTS);
	}

	public static ReportStatus aborted() {
		return new ReportStatus(Code.ABORTED);
	}

	public static ReportStatus inProgress() {
		return new ReportStatus(Code.IN_PROGRESS);
	}

	public static ReportStatus timeout() {
		return new ReportStatus(Code.TIMEOUT);
	}

	public static ReportStatus success(String url) {
		return new ReportStatus(Code.SUCCESS, url);
	}

	public static ReportStatus failure() {
		return new ReportStatus(Code.FAILURE);
	}

	public static ReportStatus testing() {
		return new ReportStatus(Code.TESTING);
	}

	public enum Code {
		SUCCESS,
		FAILURE,
		ALREADY_EXISTS,
		ABORTED,
		IN_PROGRESS,
		TIMEOUT,
		TESTING
	}
}
