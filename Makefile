.PHONY: all clean install build up down package

all: package build up logs;

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

build:
	@ docker compose build

package:
	@ mvn package -ntp -DskipTests

up:
	@ docker compose up -d

down:
	@ docker compose down

logs:
	@ docker compose logs -f
