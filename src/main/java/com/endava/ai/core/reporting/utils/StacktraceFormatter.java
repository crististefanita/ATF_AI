package com.endava.ai.core.reporting.utils;

public final class StacktraceFormatter {
    private static final int MAX_EXTERNAL_FRAMES = 6;
    private static final String INTERNAL_PACKAGE = "com.endava";

    private StacktraceFormatter() {
    }

    public static String format(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        Throwable root = findRootCause(throwable);
        StringBuilder builder = new StringBuilder();

        appendFailureChain(builder, throwable);
        appendRootMessage(builder, root);
        appendRelevantFrames(builder, root);

        return builder.toString();
    }

    private static Throwable findRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private static void appendRootMessage(StringBuilder builder, Throwable root) {
        String message = root.getMessage();
        if (message == null) {
            return;
        }

        builder.append("Root cause detail:\n")
                .append(root.getClass().getSimpleName())
                .append(": ")
                .append(message)
                .append("\n\n");
    }

    private static void appendFailureChain(StringBuilder builder, Throwable throwable) {
        builder.append("Failure chain:\n");
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < 8) {
            builder.append(depth == 0 ? "- " : "  caused by: ")
                    .append(current.getClass().getSimpleName());
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                builder.append(": ").append(firstLine(message));
            }
            builder.append("\n");
            Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }
            current = cause;
            depth++;
        }
        builder.append("\n");
    }

    private static String firstLine(String value) {
        int lineBreak = value.indexOf('\n');
        return lineBreak > 0 ? value.substring(0, lineBreak) : value;
    }

    private static void appendRelevantFrames(StringBuilder builder, Throwable root) {
        int externalCount = 0;

        for (StackTraceElement element : root.getStackTrace()) {
            String className = element.getClassName();

            if (className.startsWith(INTERNAL_PACKAGE)) {
                builder.append("at ").append(element).append("\n");
                continue;
            }

            if (externalCount < MAX_EXTERNAL_FRAMES) {
                builder.append("at ").append(element).append("\n");
                externalCount++;
            }
        }
    }
}
