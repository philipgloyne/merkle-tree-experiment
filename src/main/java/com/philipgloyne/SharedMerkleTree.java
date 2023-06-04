package com.philipgloyne;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Allows a MerkleTree to be shared between threads, writes/mutations will block reads & writes to
 * keep consistency.
 */
public class SharedMerkleTree extends MerkleTree {

    private final ReadWriteLock lock;

    public SharedMerkleTree(TreeBuilder builder, HashAlgorithm hashFn, List<String> txs) {
        super(builder, hashFn, txs);
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public boolean validateProof(int index, List<String> proof) {
        lock.readLock().lock();
        try {
            return super.validateProof(index, proof);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateTx(int index, String value) {
        lock.writeLock().lock();
        try {
            super.updateTx(index, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addTx(String value) {
        lock.writeLock().lock();
        try {
            super.addTx(value);
        } finally {
            lock.writeLock().unlock();
        }
    }

}
