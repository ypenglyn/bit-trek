.PHONY:
	help

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

build:  ## build docker image
	@cd solr-lsh && mvn clean package && cp target/solr-lsh-0.0.1-SNAPSHOT.jar ../collection/lib/ && cd ..
	@docker build -t test/bits:latest .

push:   ## push docker image
	@echo push

clean:  ## clean package binary and remove old images
	@docker rmi -f test/bits:latest

run:   ## test docker images
	@docker run -it --rm -p 8983:8983 --name bit-trek test/bits:latest

login: ## attach in docker
	@docker exec -it bit-trek bash
