package com.endava.ai.core.support.contract.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AtfCoreContract {
    String id();

    AtfCoreArea area();

    String owner();

    String invariant();

    String repairHint();

    AtfCoreSeverity severity() default AtfCoreSeverity.BLOCKER;
}
