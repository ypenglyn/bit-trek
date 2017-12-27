.PHONY:
	help

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

build:  ## build docker image
	@cd solr-lsh && mvn clean package && cp target/solr-lsh-0.0.1-SNAPSHOT.jar ../collection/lib/ && cd ..
	@docker build -f Dockerfile_SOLR -t test/bits:latest .

clean:  ## clean package binary and remove old images
	@docker rmi -f test/bits:latest

run:    ## test docker images
	@docker run -it --rm -p 8983:8983 --name bit-trek test/bits:latest

login:  ## attach in docker
	@docker exec -it bit-trek bash

example-hasher: ## build test index with mnist image
	@cd solr-lsh && mvn install && cd ..
	@cd examples/build-index/hasher && mvn clean package && cd ../..
	@docker build -f Dockerfile_JDK -t test/hasher:latest .
	@docker run -it --rm -p 8080:8080 --name hasher test/hasher:latest

example-indexer: ## index mnist image feature vector
	@docker build -f Dockerfile_PYTHON -t test/indexer:latest .
	@docker run -it --rm --name indexer test/indexer:latest

clean-example:  ## clean examples
	@docker rmi -f test/hasher:latest
	@docker rmi -f test/indexer:latest
