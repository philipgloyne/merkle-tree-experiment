package com.philipgloyne;

import java.util.List;

public interface TreeBuilder {

    List<byte[]> build(List<byte[]> txs);
}
