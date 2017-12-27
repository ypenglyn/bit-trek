# Build Index

An example for using solr-lsh to index high dimension data. There are 2 modules for building index.

- indexer
- hasher

Indexer is written by python(3.6), which will load vector data from a packed binary file. At same time,
`hasher` is sole SprintBoot application providing http based super bit hash function.

For testing purpose, indexer downloads `MNIST` dataset to local and index SB-LSH code of each handwritting image to SOLR.

## Usage

1. setup hasher
```
make example-hasher
```
2. setup indexer
```
make example-indexer
```
