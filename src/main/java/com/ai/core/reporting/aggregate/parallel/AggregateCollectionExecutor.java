package com.ai.core.reporting.aggregate.parallel;

@FunctionalInterface
public interface AggregateCollectionExecutor {
    AggregateCollectionResult execute(AggregateCollectionSpec spec) throws Exception;
}
