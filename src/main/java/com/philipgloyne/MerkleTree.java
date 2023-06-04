package com.philipgloyne;

import java.util.ArrayList;
import java.util.List;

public class MerkleTree {

    private final TreeBuilder builder;
    private final HashAlgorithm hashFn;
    private int txSize;
    private List<String> values;

    public MerkleTree(TreeBuilder builder, HashAlgorithm hashFn, List<String> txs) {
        this.builder = builder;
        this.hashFn = hashFn;
        this.txSize = txs.size();
        this.values = builder.build(txs);
    }

    public String getRoot() {
        return values.get(values.size() - 1);
    }

    /**
     * The path of hashes required to prove a transaction at an index
     *
     * @param index of tx
     * @return list of hashes which proves the transaction will lead to the merkle root
     */
    public List<String> createProof(int index) {
        return createProofIndexes(index).stream().map(i -> values.get(i)).toList();
    }

    /**
     * TRUE if the proof path and for a given index is correct, FALSE otherwise
     *
     * @param index of tx
     * @param proof
     * @return
     */
    public boolean validateProof(int index, List<String> proof) {
        return createProof(index).equals(proof);
    }

    public boolean validateProofTx(int index, List<String> proof, String tx) {
        return values.get(index).equals(hashFn.hash(tx)) && createProof(index).equals(proof);
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
            levelSize = (int) Math.ceil((levelSize + 1) >> 1);
        }
        return indexes;
    }

    private int getLeftChildIndex(int levelStart, int indexInLevel) {
        int offset = (indexInLevel % 2 == 0) ? indexInLevel : indexInLevel - 1;
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
     *
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
        int levelSize = (int) Math.ceil((txSize + 1) >> 1);
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
            levelSize = (int) Math.ceil((levelSize + 1) >> 1);
        }

        // update root
        String rootHash = hashFn.hash(values.get(values.size() - 3) + values.get(values.size() - 2));
        values.set(values.size() - 1, rootHash);
    }

    /**
     * Add a new transaction to an existing tree.
     * Reader: this in case we want a talking point regarding how computationally inefficient it is and the
     * read vs write workload of this data structure (I have never used one in production).
     *
     * @param tx - the transaction to add
     */
    public void addTx(String tx) {
        List<String> txs = values.subList(0, txSize);
        txs.add(tx);
        this.txSize = txs.size();
        this.values = builder.build(txs);
    }
}

