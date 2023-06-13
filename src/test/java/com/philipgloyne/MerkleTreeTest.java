package com.philipgloyne;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class MerkleTreeTest {

    @Test
    void testEmptyMerkleTree() {

        MerkleTree idTree = basicIdentityTree(List.of());

        assertArrayEquals("".getBytes(), idTree.getRoot());
    }

    @Test
    void testMerkleRootOneTx() {
        List<byte[]> transactions = toListByteArray("A");

        MerkleTree idTree = basicIdentityTree(transactions);
        MerkleTree sha256Tree = basicSha256Tree(transactions);

        assertArrayEquals("A".getBytes(), idTree.getRoot());
        assertEquals(
                "1cd6ef71e6e0ff46ad2609d403dc3fee244417089aa4461245a4e4fe23a55e42",
                HexFormat.of().formatHex(sha256Tree.getRoot()));
    }

    @Test
    void testMerkleRootTwoTxs() {
        List<byte[]> transactions = toListByteArray("A", "B");

        MerkleTree idTree = basicIdentityTree(transactions);
        MerkleTree sha256Tree = basicSha256Tree(transactions);

        assertArrayEquals("AB".getBytes(), idTree.getRoot());
        assertEquals("4347036f8a3cbba13f41e94672cac98c781acc2f03ab8707c4463c19d3690a5f",
                HexFormat.of().formatHex(sha256Tree.getRoot()));
    }

    @Test
    void testMerkleRootUnbalancedTree() {
        List<byte[]> transactions = toListByteArray("A", "B", "C");

        MerkleTree idTree = basicIdentityTree(transactions);
        MerkleTree sha256Tree = basicSha256Tree(transactions);

        assertArrayEquals("ABC".getBytes(), idTree.getRoot());
        assertEquals("8d3a84a14d688e0c68d0805f8c25ca52611637117f00d65eff5f1937aebd8a37",
                HexFormat.of().formatHex(sha256Tree.getRoot()));
    }

    @Test
    void testUpdateTxShouldUpdateMerkleTree() {
        List<byte[]> transactions = toListByteArray("A", "B", "C", "D", "E");


        MerkleTree idTree = basicIdentityTree(transactions);
        idTree.updateTx(0, "1".getBytes());
        assertArrayEquals("1BCDE".getBytes(), idTree.getRoot());

        idTree.updateTx(1, "2".getBytes());
        assertArrayEquals("12CDE".getBytes(), idTree.getRoot());

        idTree.updateTx(2, "3".getBytes());
        assertArrayEquals("123DE".getBytes(), idTree.getRoot());

        idTree.updateTx(3, "4".getBytes());
        assertArrayEquals("1234E".getBytes(), idTree.getRoot());

        idTree.updateTx(4, "5".getBytes());
        assertArrayEquals("12345".getBytes(), idTree.getRoot());
    }

    @Test
    void testCreateProofFourTxs() {
        List<byte[]> transactions = toListByteArray("A", "B", "C", "D");

        // A, B, C, D | AB, CD | ABCD
        // 0, 1, 2, 3 | 4,  5, | 6

        MerkleTree idTree = basicIdentityTree(transactions);

        assertListByteArray(toListByteArray("B", "CD"), idTree.createProof(0));
        assertListByteArray(toListByteArray("A", "CD"), idTree.createProof(1));
        assertListByteArray(toListByteArray("D", "AB"), idTree.createProof(2));
        assertListByteArray(toListByteArray("C", "AB"), idTree.createProof(3));
    }

    @Test
    void testCreateProofUnbalancedFiveTxs() {
        List<byte[]> transactions = toListByteArray("A", "B", "C", "D", "E");
        // A, B, C, D, E | AB, CD, E  | ABCD, E  | ABCDEF
        // 0, 1, 2, 3, 4 | 5,  6,  7, | 8,    9  | 10

        MerkleTree idTree = basicIdentityTree(transactions);

        assertListByteArray(toListByteArray("B", "CD", "E"), idTree.createProof(0));
        assertListByteArray(toListByteArray("A", "CD", "E"), idTree.createProof(1));
        assertListByteArray(toListByteArray("D", "AB", "E"), idTree.createProof(2));
        assertListByteArray(toListByteArray("C", "AB", "E"), idTree.createProof(3));
        assertListByteArray(toListByteArray("ABCD"), idTree.createProof(4));
    }

    @Test
    void testCreateProofUnbalancedNineTxs() {
        List<byte[]> transactions = toListByteArray("A", "B", "C", "D", "E", "F", "G", "H", "I");
        // A, B, C, D, E, F, G, H, I, | AB, CD, EF, GH, I, | ABCD, EFGH, I, | ABCDEFGH, I, | ABCDEFGHI

        MerkleTree idTree = basicIdentityTree(transactions);

        assertListByteArray(toListByteArray("B", "CD", "EFGH", "I"), idTree.createProof(0));
        assertListByteArray(toListByteArray("A", "CD", "EFGH", "I"), idTree.createProof(1));
        assertListByteArray(toListByteArray("D", "AB", "EFGH", "I"), idTree.createProof(2));
        assertListByteArray(toListByteArray("C", "AB", "EFGH", "I"), idTree.createProof(3));
        assertListByteArray(toListByteArray("F", "GH", "ABCD", "I"), idTree.createProof(4));
        assertListByteArray(toListByteArray("E", "GH", "ABCD", "I"), idTree.createProof(5));
        assertListByteArray(toListByteArray("H", "EF", "ABCD", "I"), idTree.createProof(6));
        assertListByteArray(toListByteArray("G", "EF", "ABCD", "I"), idTree.createProof(7));
        assertListByteArray(toListByteArray("ABCDEFGH"), idTree.createProof(8));
    }

    @Test
    void testValidateProofUnbalancedNineTxs() {
        List<byte[]> transactions = toListByteArray("A", "B", "C", "D", "E", "F", "G", "H", "I");

        MerkleTree idTree = basicIdentityTree(transactions);

        assertFalse(idTree.validateProof(0, toListByteArray("B", "CD", "EFGH")));
        assertFalse(idTree.validateProof(0, toListByteArray("A", "CD", "EFGH", "I")));
        assertFalse(idTree.validateProof(0, toListByteArray("A", "CD", "EFGH", "I", "Z")));
        assertTrue(idTree.validateProof(0, toListByteArray("B", "CD", "EFGH", "I")));

        assertFalse(idTree.validateProof(8, toListByteArray("")));
        assertTrue(idTree.validateProof(8, toListByteArray("ABCDEFGH")));
    }

    @Test
    void testValidateProofTxUnbalancedNineTxs() {
        List<byte[]> transactions = toListByteArray("A", "B", "C", "D", "E", "F", "G", "H", "I");

        MerkleTree idTree = basicIdentityTree(transactions);

        assertFalse(idTree.validateProofTx(0, toListByteArray("B", "CD", "EFGH", "I"), "Z".getBytes()));
        assertTrue(idTree.validateProofTx(0, toListByteArray("B", "CD", "EFGH", "I"), "A".getBytes()));

        assertFalse(idTree.validateProofTx(8, toListByteArray(""), "I".getBytes()));
        assertTrue(idTree.validateProofTx(8, toListByteArray("ABCDEFGH"), "I".getBytes()));
    }


    @Test
    void testAddTxRebuildsTree() {
        List<byte[]> transactions = toListByteArray("A", "B", "C", "D");
        MerkleTree idTree = basicIdentityTree(transactions);
        assertArrayEquals("ABCD".getBytes(), idTree.getRoot());

        idTree.addTx("E".getBytes());

        assertArrayEquals("ABCDE".getBytes(), idTree.getRoot());
        assertListByteArray(toListByteArray("ABCD"), idTree.createProof(4));
        assertTrue(idTree.validateProof(4, toListByteArray("ABCD")));
    }

    private class IdentityHash implements HashAlgorithm {
        @Override
        public byte[] hash(byte[] s) {
            return s;
        }
    }

    private List<byte[]> toListByteArray(String ... arr) {
        return Stream.of(arr).map(String::getBytes).toList();
    }

    private void assertListByteArray(List<byte[]> expect, List<byte[]> actual) {
        for (int i = 0; i < expect.size(); i++) {
            assertArrayEquals(expect.get(i), actual.get(i),
                    "expect '" + new String(expect.get(i)) + "' but was '" + new String(actual.get(i)) + "'");
        }
    }

    private MerkleTree basicIdentityTree(List<byte[]> txs) {
        IdentityHash hashFn = new IdentityHash();
        BasicTreeBuilder builder = new BasicTreeBuilder(hashFn);
        return new MerkleTree(builder, hashFn, txs);
    }

    private MerkleTree basicSha256Tree(List<byte[]> txs) {
        SHA256D hashFn = new SHA256D();
        BasicTreeBuilder builder = new BasicTreeBuilder(hashFn);
        return new MerkleTree(builder, hashFn, txs);
    }


}
