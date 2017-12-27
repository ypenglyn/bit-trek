# BIT-TREK

`bit-trek`, a plugin of `SOLR` for similarity search.

## Motivation
Building a efficient similarity search with scalability is quite time consumable. 
Keeping search performance stable while data increase is even delicate work. In my case, billion scale similarity search for images with commodity hardware is base challenge.
At the same time, embedding a images similarity into full text retrieval also worth to try. With these in mind, I explored a approach for indexing dense vectors and embedding cosine similarity of vectors 
into solr IR model.

## Building

Start a solr docker container with `bit-trek`.
```
make build && make run
```
For more details about index data with `bit-trek`, please refer to examples.

## How BIT-TREK works

### Query example
```
curl http://your_solr_host:8983/solr/bits/select?
        fl=id, $x
        &x=cos(bits,'YOUR_HASH_FOR_QUERY')
        &sort=$x desc
        &rows=20
        &q=bits_match_4:YOUR_HASH_FOR_QUERY
```
Given a SB-LSH string `YOUR_HASH_FOR_QUERY`, function `cos` calculates cosine similarity between field `bits` and `YOUR_HASH_FOR_QUERY`. `bits_match_4` is a 4-grams field of `bits`.
With `n-gram` fields like `bits_match_4`, it is possible to unilize inversed document index for searching similar vectors like text search.

### Query metrics

 - query performance
 - query scalability
 - embedding query with text

## Contact
It is always welcome for any improving ideas and issue feedback.

 - Yanpeng Lin, ypenglyn@gmail.com

## License

This work is Apache License Version 2.0 licensed.
