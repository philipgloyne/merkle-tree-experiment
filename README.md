# Merkle Tree Experiment

Implementation of a Merkle-Tree in Java.

#### Supported features

- Create a Merkle tree with a list of transactions
- Return the Merkle root
- Create a Merkle proof, a path (in the form of a List<String>) to the root
- Validate a given Merkle proof
- Update a single transaction in a tree 
- Add a transaction to a tree

#### Design notes

- Hash function is dependency injected (SHA256D or IdentityHash for tests)
- Backed by an Arraylist to increase proof/verify (read) performance.

#### Run notes

- Java 17+, minimal dependency library (junit) 
- Gradle build `gradle test`

