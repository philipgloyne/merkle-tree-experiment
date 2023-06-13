package com.philipgloyne;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class MerkleTreeTest {

    @Test
    void testEmptyMerkleTree() {

        MerkleTree idTree = basicIdentityTree(List.of());

        assertEquals("", idTree.getRoot());
    }

    @Test
    void testMerkleRootOneTx() {
        List<String> transactions = List.of("A");

        MerkleTree idTree = basicIdentityTree(transactions);
        MerkleTree sha256Tree = basicSha256Tree(transactions);

        assertEquals("A", idTree.getRoot());
        assertEquals("1cd6ef71e6e0ff46ad2609d403dc3fee244417089aa4461245a4e4fe23a55e42", sha256Tree.getRoot());
    }

    @Test
    void testMerkleRootTwoTxs() {
        List<String> transactions = Arrays.asList("A", "B");

        MerkleTree idTree = basicIdentityTree(transactions);
        MerkleTree sha256Tree = basicSha256Tree(transactions);

        assertEquals("AB", idTree.getRoot());
        assertEquals("c25e7a4d9ec0fc3261d86909663c5292119e1682efac28c830abcd5f3ad8aab1", sha256Tree.getRoot());
    }

    @Test
    void testMerkleRootUnbalancedTree() {
        List<String> transactions = Arrays.asList("A", "B", "C");

        MerkleTree idTree = basicIdentityTree(transactions);
        MerkleTree sha256Tree = basicSha256Tree(transactions);

        assertEquals("ABC", idTree.getRoot());
        assertEquals("b998db2124e4f2dce45a1711bf04f8cba6b91a6567201bb7e0db3aa7e7e63044", sha256Tree.getRoot());
    }

    @Test
    void testUpdateTxShouldUpdateMerkleTree() {
        List<String> transactions = Arrays.asList("A", "B", "C", "D", "E");

        MerkleTree idTree = basicIdentityTree(transactions);
        idTree.updateTx(0, "1");
        assertEquals("1BCDE", idTree.getRoot());

        idTree.updateTx(1, "2");
        assertEquals("12CDE", idTree.getRoot());

        idTree.updateTx(2, "3");
        assertEquals("123DE", idTree.getRoot());

        idTree.updateTx(3, "4");
        assertEquals("1234E", idTree.getRoot());

        idTree.updateTx(4, "5");
        assertEquals("12345", idTree.getRoot());
    }

    @Test
    void testCreateProofFourTxs() {
        List<String> transactions = Arrays.asList("A", "B", "C", "D");
        // A, B, C, D | AB, CD | ABCD
        // 0, 1, 2, 3 | 4,  5, | 6

        MerkleTree idTree = basicIdentityTree(transactions);

        assertEquals(Arrays.asList("B", "CD"), idTree.createProof(0));
        assertEquals(Arrays.asList("A", "CD"), idTree.createProof(1));
        assertEquals(Arrays.asList("D", "AB"), idTree.createProof(2));
        assertEquals(Arrays.asList("C", "AB"), idTree.createProof(3));
    }

    @Test
    void testCreateProofUnbalancedFiveTxs() {
        List<String> transactions = Arrays.asList("A", "B", "C", "D", "E");
        // A, B, C, D, E | AB, CD, E  | ABCD, E  | ABCDEF
        // 0, 1, 2, 3, 4 | 5,  6,  7, | 8,    9  | 10

        MerkleTree idTree = basicIdentityTree(transactions);

        assertEquals(Arrays.asList("B", "CD", "E"), idTree.createProof(0));
        assertEquals(Arrays.asList("A", "CD", "E"), idTree.createProof(1));
        assertEquals(Arrays.asList("D", "AB", "E"), idTree.createProof(2));
        assertEquals(Arrays.asList("C", "AB", "E"), idTree.createProof(3));
        assertEquals(Arrays.asList("ABCD"), idTree.createProof(4));
    }

    @Test
    void testCreateProofUnbalancedNineTxs() {
        List<String> transactions = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I");
        // A, B, C, D, E, F, G, H, I, | AB, CD, EF, GH, I, | ABCD, EFGH, I, | ABCDEFGH, I, | ABCDEFGHI

        MerkleTree idTree = basicIdentityTree(transactions);

        assertEquals(Arrays.asList("B", "CD", "EFGH", "I"), idTree.createProof(0));
        assertEquals(Arrays.asList("A", "CD", "EFGH", "I"), idTree.createProof(1));
        assertEquals(Arrays.asList("D", "AB", "EFGH", "I"), idTree.createProof(2));
        assertEquals(Arrays.asList("C", "AB", "EFGH", "I"), idTree.createProof(3));
        assertEquals(Arrays.asList("F", "GH", "ABCD", "I"), idTree.createProof(4));
        assertEquals(Arrays.asList("E", "GH", "ABCD", "I"), idTree.createProof(5));
        assertEquals(Arrays.asList("H", "EF", "ABCD", "I"), idTree.createProof(6));
        assertEquals(Arrays.asList("G", "EF", "ABCD", "I"), idTree.createProof(7));
        assertEquals(Arrays.asList("ABCDEFGH"), idTree.createProof(8));
    }

    @Test
    void testValidateProofUnbalancedNineTxs() {
        List<String> transactions = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I");

        MerkleTree idTree = basicIdentityTree(transactions);

        assertFalse(idTree.validateProof(0, Arrays.asList("B", "CD", "EFGH")));
        assertFalse(idTree.validateProof(0, Arrays.asList("A", "CD", "EFGH", "I")));
        assertFalse(idTree.validateProof(0, Arrays.asList("A", "CD", "EFGH", "I", "Z")));
        assertTrue(idTree.validateProof(0, Arrays.asList("B", "CD", "EFGH", "I")));

        assertFalse(idTree.validateProof(8, Arrays.asList("")));
        assertTrue(idTree.validateProof(8, Arrays.asList("ABCDEFGH")));
    }

    @Test
    void testValidateProofTxUnbalancedNineTxs() {
        List<String> transactions = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I");

        MerkleTree idTree = basicIdentityTree(transactions);

        assertFalse(idTree.validateProofTx(0, Arrays.asList("B", "CD", "EFGH", "I"), "Z"));
        assertTrue(idTree.validateProofTx(0, Arrays.asList("B", "CD", "EFGH", "I"), "A"));

        assertFalse(idTree.validateProofTx(8, Arrays.asList(""), "I"));
        assertTrue(idTree.validateProofTx(8, Arrays.asList("ABCDEFGH"), "I"));
    }


    @Test
    void testAddTxRebuildsTree() {
        List<String> transactions = Arrays.asList("A", "B", "C", "D");
        MerkleTree idTree = basicIdentityTree(transactions);
        assertEquals("ABCD", idTree.getRoot());

        idTree.addTx("E");

        assertEquals("ABCDE", idTree.getRoot());
        assertEquals(List.of("ABCD"), idTree.createProof(4));
        assertTrue(idTree.validateProof(4, List.of("ABCD")));
    }

    private class IdentityHash implements HashAlgorithm {
        @Override
        public String hash(String s) {
            return s;
        }
    }

    private MerkleTree basicIdentityTree(List<String> txs) {
        IdentityHash hashFn = new IdentityHash();
        BasicTreeBuilder builder = new BasicTreeBuilder(hashFn);
        return new MerkleTree(builder, hashFn, txs);
    }

    private MerkleTree basicSha256Tree(List<String> txs) {
        SHA256D hashFn = new SHA256D();
        BasicTreeBuilder builder = new BasicTreeBuilder(hashFn);
        return new MerkleTree(builder, hashFn, txs);
    }


}
