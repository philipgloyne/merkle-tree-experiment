package com.philipgloyne;

import java.util.ArrayList;
import java.util.List;

public class BasicTreeBuilder implements TreeBuilder {

    private final HashAlgorithm hashFn;

    public BasicTreeBuilder(HashAlgorithm hashFn) {
        this.hashFn = hashFn;
    }

    public List<String> build(List<String> txs) {
        if (txs.size() == 0) return List.of("");
        if (txs.size() == 1) return List.of(hashFn.hash(txs.get(0)));

        List<String> levels = new ArrayList<>(txs);
        List<String> currentLvl = txs;
        while (currentLvl.size() > 1) {
            List<String> nextLvl = new ArrayList<>();

            for (int i = 0; i < currentLvl.size(); i += 2) {

                String hash1 = hashFn.hash(currentLvl.get(i));
                String hash2 = (i + 1 < currentLvl.size())
                        ? hashFn.hash(currentLvl.get(i + 1))
                        : "";

                String parentHash = hashFn.hash(hash1 + hash2);

                nextLvl.add(parentHash);
                levels.add(parentHash);
            }

            currentLvl = nextLvl;
        }

        // System.out.println("buildTree: " + Arrays.toString(levels.toArray()));

        return levels;
    }

}
