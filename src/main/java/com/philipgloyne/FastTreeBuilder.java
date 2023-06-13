package com.philipgloyne;

import org.apache.commons.lang3.ArrayUtils;

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
    public List<byte[]> build(List<byte[]> txs) {
        if (txs.size() == 0) return List.of("".getBytes());
        if (txs.size() == 1) return List.of(hashFn.hash(txs.get(0)));

        List<byte[]> levels = new ArrayList<>(txs);
        List<byte[]> currentLvl = txs;
        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);

        while (currentLvl.size() > 1) {
            List<byte[]> nextLvl = new ArrayList<>();
            List<Future<byte[]>> futures = new ArrayList<>();

            for (int i = 0; i < currentLvl.size(); i += 2) {
                final int finalI = i;

                List<byte[]> finalCurrentLvl = currentLvl;
                Callable<byte[]> task = () -> {
                    byte[] hash1 = hashFn.hash(finalCurrentLvl.get(finalI));
                    byte[] hash2 = (finalI + 1 < finalCurrentLvl.size())
                            ? hashFn.hash(finalCurrentLvl.get(finalI + 1))
                            : "".getBytes();
                    return hashFn.hash(ArrayUtils.addAll(hash1, hash2));
                };

                Future<byte[]> future = executorService.submit(task);
                futures.add(future);
            }

            for (Future<byte[]> future : futures) {
                try {
                    byte[] parentHash = future.get();
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

