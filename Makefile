.PHONY: all clean install build up down package

all: package docker up logs;

build: prettier package docker

clean:
	@ mvn clean
	@ docker volume prune -f
	@ docker system prune -f

install:
	@ mvn clean install

verify:
	@ mvn verify

test: install up
	@ mvn verify

healthcheck:
	@ cd cucumber-tests && sh healthcheck.sh

bdd: healthcheck
	@ cd cucumber-tests && mvn verify -ntp -Dcucumber.filter.tags="@web"

docker:
	@ docker compose build

prettier:
	@ mvn prettier:write

package:
	@ mvn package -ntp -DskipTests

docker: package build

up:
	@ docker compose up -d

stop:
	@ docker compose stop

down:
	@ docker compose down

logs:
	@ docker compose logs -f
