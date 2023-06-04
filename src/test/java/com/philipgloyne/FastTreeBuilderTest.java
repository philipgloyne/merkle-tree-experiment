package com.philipgloyne;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

class FastTreeBuilderTest {

    @Test
    void testMultiThreadedTreeBuilder() {

        String aLongRandomString = randomString(50000);
        List<String> transactions = IntStream
                .rangeClosed(1, 10000)
                .boxed()
                .map(v -> v + aLongRandomString)
                .toList();

        long t0 = System.currentTimeMillis();
        BasicTreeBuilder builder = new BasicTreeBuilder(new SHA256D());
        builder.build(transactions);
        long t1 = System.currentTimeMillis();

        System.out.println("basic tree: " + (t1 - t0) + "ms");

        t0 = System.currentTimeMillis();
        FastTreeBuilder fastTreeBuilder = new FastTreeBuilder(new SHA256D());
        fastTreeBuilder.build(transactions);
        t1 = System.currentTimeMillis();

        System.out.println("fast tree: " + (t1 - t0) + "ms"); // 8 threads
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
