package com.bawnorton.neruina.version;

import com.bawnorton.neruina.Neruina;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionString {
    private final String[] parts;
    private final VersionComparison comparison;

    public VersionString(String versionString) {
        this.comparison = validateVersionString(versionString);
        this.parts = versionString.substring(comparison.toString().length()).split("\\.");
    }

    private static VersionComparison validateVersionString(String versionString) {
        if (versionString == null) {
            throw new IllegalArgumentException("Version string cannot be null");
        }
        Pattern pattern = Pattern.compile(".+?(?=[0-9])");
        Matcher matcher = pattern.matcher(versionString);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid version string: " + versionString);
        }
        String versionComparison = matcher.group(0);
        VersionComparison comparison = VersionComparison.fromString(versionComparison);
        if (comparison == null) {
            throw new IllegalArgumentException("Invalid version comparison: " + versionComparison);
        }
        return comparison;
    }

    public String[] getParts() {
        return parts;
    }

    public VersionComparison getComparison() {
        return comparison;
    }

    public boolean isVersionValid(String version) {
        String[] versionParts = version.split("\\.");
        String[] parts = getParts().clone();
        while (versionParts.length != parts.length) {
            if (versionParts.length < parts.length) {
                String[] newVersionParts = new String[versionParts.length + 1];
                System.arraycopy(versionParts, 0, newVersionParts, 0, versionParts.length);
                newVersionParts[versionParts.length] = "0";
                versionParts = newVersionParts;
            } else {
                String[] newVersionStringParts = new String[parts.length + 1];
                System.arraycopy(parts, 0, newVersionStringParts, 0, parts.length);
                newVersionStringParts[parts.length] = "0";
                parts = newVersionStringParts;
            }
        }
        for (int i = 0; i < parts.length; i++) {
            int versionPart;
            int part;
            try {
                versionPart = Integer.parseInt(versionParts[i]);
                part = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                Neruina.LOGGER.warn("Could not parse version string: " + version + " or version string is for a non-release version, which is not supported.");
                return false;
            }
            if (versionPart == part) continue;

            switch (getComparison()) {
                case EQUALS -> {
                    return false;
                }
                case GREATER_THAN -> {
                    return versionPart > part;
                }
                case LESS_THAN -> {
                    return versionPart < part;
                }
                case GREATER_THAN_OR_EQUAL_TO -> {
                    return versionPart >= part;
                }
                case LESS_THAN_OR_EQUAL_TO -> {
                    return versionPart <= part;
                }
            }
        }
        return true;
    }
}
