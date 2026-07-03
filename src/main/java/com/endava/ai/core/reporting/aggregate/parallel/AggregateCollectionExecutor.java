package com.endava.ai.core.reporting.aggregate.parallel;

@FunctionalInterface
public interface AggregateCollectionExecutor {
    AggregateCollectionResult execute(AggregateCollectionSpec spec) throws Exception;
}
