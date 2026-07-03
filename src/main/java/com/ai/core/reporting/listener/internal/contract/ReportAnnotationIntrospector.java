package com.ai.core.reporting.listener.internal.contract;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

final class ReportAnnotationIntrospector {
    private ReportAnnotationIntrospector() {
    }

    static Annotation find(Annotation[] annotations, String annotationName) {
        // Keep the production listener independent from test-scope contract annotations.
        for (Annotation annotation : annotations) {
            if (annotationName.equals(annotation.annotationType().getName())) {
                return annotation;
            }
        }
        return null;
    }

    static String readString(Annotation annotation, String member) {
        try {
            Object value = annotation.annotationType().getMethod(member).invoke(annotation);
            return String.valueOf(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Unable to read @" + annotation.annotationType().getSimpleName() + "." + member,
                    e
            );
        }
    }

    static Object invokeStatic(String className, String method, Class<?> parameterType, Object argument)
            throws ReflectiveOperationException {
        Class<?> catalogClass = Class.forName(className);
        return catalogClass.getMethod(method, parameterType).invoke(null, argument);
    }

    static String invokeString(Object target, String method) throws ReflectiveOperationException {
        Object value = target.getClass().getMethod(method).invoke(target);
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    static List<String> invokeStringList(Object target, String method) throws ReflectiveOperationException {
        Object value = target.getClass().getMethod(method).invoke(target);
        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }
        return List.of(String.valueOf(value));
    }
}
