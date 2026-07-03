package com.endava.ai.atf.core.support.contracts.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Set;

final class AnnotationSpec {
    private final Class<? extends Annotation> type;
    private final Set<ElementType> targets;

    AnnotationSpec(Class<? extends Annotation> type, Set<ElementType> targets) {
        this.type = type;
        this.targets = targets;
    }

    Class<? extends Annotation> type() {
        return type;
    }

    Set<ElementType> targets() {
        return targets;
    }
}
