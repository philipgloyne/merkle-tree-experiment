package com.philipgloyne;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FastTreeBuilderTest {

    @Test
    void testMultiThreadedTreeBuilder() {

        String aLongRandomString = randomString(1200); // 4 bytes/character (UTF-8) * 300 bytes (eth tx)
        List<byte[]> transactions = IntStream
                .rangeClosed(1, 10000)
                .boxed()
                .map(v -> (v + aLongRandomString).getBytes())
                .toList();

        long t0 = System.currentTimeMillis();
        BasicTreeBuilder builder = new BasicTreeBuilder(new SHA256D());
        builder.build(transactions);
        long t1 = System.currentTimeMillis();
        long basicBuildTime = t1 - t0;

        System.out.println("basic tree: " + basicBuildTime + "ms");

        long t2 = System.currentTimeMillis();
        FastTreeBuilder fastTreeBuilder = new FastTreeBuilder(new SHA256D()); // 8 threads
        fastTreeBuilder.build(transactions);
        long t3 = System.currentTimeMillis();
        long fastBuildTime = t3 - t2;

        System.out.println("fast tree: " + fastBuildTime + "ms");

        assertTrue(basicBuildTime > fastBuildTime);
    }

    public String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            int index = (int) (alphaNumericString.length() * Math.random());
            sb.append(alphaNumericString.charAt(index));
        }
        return sb.toString();
    }

}
