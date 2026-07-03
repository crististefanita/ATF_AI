package com.endava.ai.skills.mcp.provenance;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class McpExtentOriginCommand {
    private McpExtentOriginCommand() {
    }

    static boolean looksLikeMavenTestCommand(String command) {
        List<String> tokens = tokens(command);
        boolean usesMaven = tokens.stream().anyMatch(token ->
                token.equals("mvn") || token.equals("mvnw") || token.equals("mvn.cmd") || token.equals("./mvnw"));
        boolean invokesTestLifecycle = tokens.stream().anyMatch("test"::equals);
        return usesMaven && invokesTestLifecycle && !disablesTests(tokens);
    }

    static boolean hasConcreteTestSelector(String command) {
        return tokens(command).stream().anyMatch(token ->
                token.startsWith("-dtest=")
                        || token.startsWith("-dsurefire.suitexmlfiles=")
                        || token.startsWith("-dfailsafe.suitexmlfiles="));
    }

    private static List<String> tokens(String command) {
        if (command == null || command.isBlank()) {
            return List.of();
        }
        String normalized = command.toLowerCase(Locale.ROOT)
                .replace('"', ' ')
                .replace('\'', ' ')
                .replace("(", " ")
                .replace(")", " ")
                .replace("&&", " ")
                .replace(";", " ");
        return Stream.of(normalized.split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toList());
    }

    private static boolean disablesTests(List<String> tokens) {
        return tokens.stream().anyMatch(token ->
                token.equals("-dskiptests")
                        || token.equals("-dskiptests=true")
                        || token.equals("-dmaven.test.skip")
                        || token.equals("-dmaven.test.skip=true"));
    }
}
