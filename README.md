# BIT-TREK

`bit-trek`, finding nearest neighbor for vectors utilizing `Solr`.

 - calculate cosine similarity inside Solr
 - scale-out easily with solr cloud

# Example

#### Setup single solr for testing
```
make build && make run
```

#### Indexing and Searching

indexing
```
# start hasher

$ cd build-index/hasher
# java -jar target/hasher-0.0.1-SNAPSHOT.jar
```

searching
```
http://your_solr_host:8983/solr/bits/select?fl=id,$x&x=cos(bits,'PUT_YOUR_HASH_FOR_QUERY_HERE')&sort=$st%20desc&rows=20&q=bits_match_4:PUT_YOUR_HASH_FOR_QUERY_HERE
```

 
