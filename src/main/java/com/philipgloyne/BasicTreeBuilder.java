package com.philipgloyne;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a MerkleTree and returns underlying data structure (Arraylist)
 */
public class BasicTreeBuilder implements TreeBuilder {

    private final HashAlgorithm hashFn;

    public BasicTreeBuilder(HashAlgorithm hashFn) {
        this.hashFn = hashFn;
    }

    public List<byte[]> build(List<byte[]> txs) {
        if (txs.size() == 0) return List.of("".getBytes());
        if (txs.size() == 1) return List.of(hashFn.hash(txs.get(0)));

        List<byte[]> levels = new ArrayList<>(txs);
        List<byte[]> currentLvl = txs;
        while (currentLvl.size() > 1) {
            List<byte[]> nextLvl = new ArrayList<>();

            for (int i = 0; i < currentLvl.size(); i += 2) {

                byte[] hash1 = hashFn.hash(currentLvl.get(i));
                byte[] hash2 = (i + 1 < currentLvl.size())
                        ? hashFn.hash(currentLvl.get(i + 1))
                        : "".getBytes();

                byte[] parentHash = hashFn.hash(ArrayUtils.addAll(hash1, hash2));

                nextLvl.add(parentHash);
                levels.add(parentHash);
            }

            currentLvl = nextLvl;
        }

        // System.out.println("buildTree: " + Arrays.toString(levels.toArray()));

        return levels;
    }

}
