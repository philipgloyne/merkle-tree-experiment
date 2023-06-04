package com.philipgloyne;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Tree builder that allows for concurrent hashing of parent nodes. It will complete a tree level before
 * progressing to the next.
 */
public class FastTreeBuilder implements TreeBuilder {

    private final HashAlgorithm hashFn;

    public FastTreeBuilder(HashAlgorithm hashFn) {
        this.hashFn = hashFn;
    }

    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    @Override
    public List<String> build(List<String> txs) {
        if (txs.size() == 0) return List.of("");
        if (txs.size() == 1) return List.of(hashFn.hash(txs.get(0)));

        List<String> levels = new ArrayList<>(txs);
        List<String> currentLvl = txs;
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);

        while (currentLvl.size() > 1) {
            List<String> nextLvl = new ArrayList<>();
            List<Future<String>> futures = new ArrayList<>();

            for (int i = 0; i < currentLvl.size(); i += 2) {
                final int finalI = i;

                List<String> finalCurrentLvl = currentLvl;
                Callable<String> task = () -> {
                    String hash1 = hashFn.hash(finalCurrentLvl.get(finalI));
                    String hash2 = (finalI + 1 < finalCurrentLvl.size())
                            ? hashFn.hash(finalCurrentLvl.get(finalI + 1))
                            : "";
                    return hashFn.hash(hash1 + hash2);
                };

                Future<String> future = executorService.submit(task);
                futures.add(future);
            }

            for (Future<String> future : futures) {
                try {
                    String parentHash = future.get();
                    nextLvl.add(parentHash);
                    levels.add(parentHash);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    // Handle exceptions as needed
                }
            }

            currentLvl = nextLvl;
        }

        executorService.shutdown();

        // System.out.println("buildTree: " + Arrays.toString(levels.toArray()));

        return levels;
    }
}

