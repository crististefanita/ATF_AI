package com.endava.ai.core.reporting.aggregate.parallel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class AggregateParallelCollectionRunner {
    private final AggregateCollectionExecutor executor;

    public AggregateParallelCollectionRunner(AggregateCollectionExecutor executor) {
        this.executor = executor;
    }

    public List<AggregateCollectionResult> runAll(List<AggregateCollectionSpec> specs) {
        List<AggregateCollectionSpec> orderedSpecs = validate(specs);
        return runAll(orderedSpecs, orderedSpecs.size());
    }

    public List<AggregateCollectionResult> runAll(List<AggregateCollectionSpec> specs, int maxWorkers) {
        List<AggregateCollectionSpec> orderedSpecs = validate(specs);
        int workerCount = workerCount(orderedSpecs, maxWorkers);
        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        try {
            return executeAll(orderedSpecs, pool);
        } finally {
            pool.shutdownNow();
        }
    }

    private List<AggregateCollectionResult> executeAll(
            List<AggregateCollectionSpec> orderedSpecs,
            ExecutorService pool
    ) {
        List<Future<AggregateCollectionResult>> futures = new ArrayList<>();
        for (AggregateCollectionSpec spec : orderedSpecs) {
            futures.add(pool.submit(() -> executor.execute(spec)));
        }
        List<AggregateCollectionResult> results = new ArrayList<>();
        for (Future<AggregateCollectionResult> future : futures) {
            results.add(await(future));
        }
        return results;
    }

    private static List<AggregateCollectionSpec> validate(List<AggregateCollectionSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            throw new IllegalArgumentException("At least one aggregate collection spec is required.");
        }
        Set<Path> roots = new LinkedHashSet<>();
        for (AggregateCollectionSpec spec : specs) {
            if (!roots.add(spec.outputRoot())) {
                throw new IllegalArgumentException(
                        "Aggregate collection output roots must be unique: " + spec.outputRoot()
                );
            }
        }
        return List.copyOf(specs);
    }

    private static int workerCount(List<AggregateCollectionSpec> orderedSpecs, int maxWorkers) {
        if (maxWorkers < 1) {
            throw new IllegalArgumentException("Aggregate collection maxWorkers must be at least 1.");
        }
        return Math.min(maxWorkers, orderedSpecs.size());
    }

    private static AggregateCollectionResult await(Future<AggregateCollectionResult> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for aggregate collection.", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Aggregate collection failed before it could publish a result.",
                    exception.getCause());
        }
    }
}
