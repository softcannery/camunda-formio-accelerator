.PHONY: all clean install build up down package

all: install build;

clean:
	@ mvn clean
	@ docker volume prune -f
	@ docker system prune -f

install:
	@ mvn clean install

build:
	@ docker compose build

package:
	@ mvn package -ntp

up:
	@ docker compose up -d

down:
	@ docker compose down

logs:
	@ docker compose logs -f