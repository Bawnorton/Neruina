package com.bawnorton.neruina.version;

public enum VersionComparison {
    EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUAL_TO,
    LESS_THAN_OR_EQUAL_TO;

    public static VersionComparison fromString(String group) {
        return switch (group) {
            case "=" -> EQUALS;
            case ">" -> GREATER_THAN;
            case "<" -> LESS_THAN;
            case ">=" -> GREATER_THAN_OR_EQUAL_TO;
            case "<=" -> LESS_THAN_OR_EQUAL_TO;
            default -> null;
        };
    }

    public String toString() {
        return switch (this) {
            case EQUALS -> "=";
            case GREATER_THAN -> ">";
            case LESS_THAN -> "<";
            case GREATER_THAN_OR_EQUAL_TO -> ">=";
            case LESS_THAN_OR_EQUAL_TO -> "<=";
        };
    }
}
