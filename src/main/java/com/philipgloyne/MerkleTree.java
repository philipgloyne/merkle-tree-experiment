package com.philipgloyne;

import java.util.ArrayList;
import java.util.List;

public class MerkleTree {

    private HashTransform hashFn;
    private List<String> values;
    private int txSize;

    public MerkleTree(HashTransform hashFn, List<String> txs) {
        this.hashFn = hashFn;
        this.values = buildTree(txs);
    }

    public String getRoot() {
        return values.get(values.size() - 1);
    }

    private List<String> buildTree(List<String> txs) {
        if (txs.size() == 0) return List.of("");
        if (txs.size() == 1) return List.of(hashFn.hash(txs.get(0)));

        this.txSize = txs.size();
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

    public List<String> createProof(int index) {
        return createProofIndexes(index).stream().map(i -> values.get(i)).toList();
    }

    /**
     * Returns true if the proof path and for a given
     * @param index
     * @param proof
     * @return
     */
    public boolean validateProof(int index, List<String> proof) {
        return createProof(index).equals(proof);
    }

    private List<Integer> createProofIndexes(int index) {
        List<Integer> indexes = new ArrayList<Integer>();
        int treeSize = values.size();
        int treeHeight = (int) (Math.log(treeSize) / Math.log(2));
        int levelStart = 0;
        int levelSize = txSize;
        int levelIndex = index;

        for (int level = 0; level < treeHeight; level++) {
            int leftIndex = getLeftChildIndex(levelStart, levelIndex);
            int rightIndex = getRightChildIndex(levelStart, levelIndex);

            if (rightIndex >= treeSize) {
                break;
            }

            int levelMax = levelStart + levelSize;
            if (rightIndex < levelMax) {
                if (levelIndex % 2 == 0) {
                    indexes.add(rightIndex);
                } else {
                    indexes.add(leftIndex);
                }
            }

            levelIndex = getParentIndex(levelIndex);
            levelStart += levelSize;
            levelSize = (int) Math.ceil((levelSize + 1) / 2);
        }
        return indexes;
    }

    private int getLeftChildIndex(int levelStart, int indexInLevel) {
        int offset = (indexInLevel % 2 == 0) ? indexInLevel : indexInLevel - 1 ;
        return levelStart + offset;
    }

    private int getRightChildIndex(int levelStart, int indexInLevel) {
        return getLeftChildIndex(levelStart, indexInLevel) + 1;
    }

    private int getParentIndex(int currentIndex) {
        return currentIndex / 2;
    }

    /**
     * Updates a single transaction at index and rehashes only the required nodes all the way to the root.
     * @param index - the known index of the transaction to mutate.
     * @param value - the new value of the transaction.
     */
    public void updateTx(int index, String value) {
        String hash = hashFn.hash(value);
        // update level 0
        values.set(index, hash);
        String parentHash = (index % 2 == 0)
                ? (index + 1 < txSize) ? hashFn.hash(hash + values.get(index + 1)) : hash
                : hashFn.hash(values.get(index - 1) + hash);

        List<Integer> indexes = new ArrayList<Integer>();
        int treeSize = values.size();
        int treeHeight = (int) (Math.log(treeSize) / Math.log(2));
        int levelStart = txSize;
        int levelSize = (int) Math.ceil((txSize + 1) / 2);
        int levelIndex = getParentIndex(index);

        for (int level = 1; level < treeHeight; level++) {
            int leftIndex = getLeftChildIndex(levelStart, levelIndex);
            int rightIndex = getRightChildIndex(levelStart, levelIndex);

            if (rightIndex >= treeSize) {
                break;
            }

            int levelMax = levelStart + levelSize;
            if (rightIndex < levelMax) {
                if (levelIndex % 2 == 0) {
                    values.set(leftIndex, parentHash);
                    parentHash = hashFn.hash(parentHash + values.get(rightIndex));
                } else {
                    values.set(rightIndex, parentHash);
                    parentHash = hashFn.hash(values.get(leftIndex) + parentHash);
                }
            }

            levelIndex = getParentIndex(levelIndex);
            levelStart += levelSize;
            levelSize = (int) Math.ceil((levelSize + 1) / 2);
        }

        // update root
        String rootHash = hashFn.hash(values.get(values.size() - 3) + values.get(values.size() - 2));
        values.set(values.size() - 1, rootHash);
    }

    /**
     * Add a new transaction to an existing tree.
     * Reader: this in case we want a talking point regarding how computationally inefficient it is and the
     * read vs write workload of this data structure (I have never used one in production).
     * @param tx - the transaction to add
     */
    public void addTx(String tx) {
        List<String> txs = values.subList(0, txSize);
        txs.add(tx);
        this.values = buildTree(txs);
    }
}

