# Indexer

This toy python script is used for indexing `MNIST` data to SOLR with Super Bit LSH (SB-LSH).
For more information about `SB-LSH`, please refer to solr-lsh. 

 - request `hasher` to get SB-LSH in term of base64 string
 - index `id` and `bits` to SOLR
 
 Where `id` is unique key for data inside SOLR. SOLR index SB-LSH in `bits` field and generate 3-grams, 4-grams and 3|4-grams to
 `bits_match_3`, `bits_match_4` and `bits_match_34` at the same time.
 
 ## Usage
 
 #### Local
 
```
$ python indexer.py
```

`*` NOTE: change SOLR and hasher end point accordingly.

#### Docker

```
make example-indexer
```