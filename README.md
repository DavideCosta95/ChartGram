# ChartGram

## Description

ChartGram is a Telegram bot implementation, which collects data from the groups it's added as administrator and generates charts and graphs for these groups' administrators.
It also offers an authentication-based API to serve this data to external systems.
ChartGram consists in:
- a Telegram bot, used to collect data, to generate rendered images of charts and graphs and to obtain authorization tokens to view analytics on the webapp.
- a webapp, which offers various charts and graphs about a given group, retrieving data from the API.
- a REST API, which serves authenticated requests over HTTP, using serialized JSONs.

More details, about the implemented use-cases, available in the [user guide](/assets/docs/user_guide.md).
To test the application with the pre-loaded example data, follow the [test mode](/assets/docs/user_guide.md#test-mode) section of the guide.

## Usage

- ### Database setup
    - Run `docker-compose up -d` from the project root to run a local ready-to-use PostgreSQL DBMS instance, with also
      pre-loaded example data.
    - To use a custom RDBMS instance see [advanced setup](/assets/docs/advanced_setup.md).

- ### Run
    - #### From distributable jar
        - Download the [latest stable release](https://github.com/DavideCosta95/ChartGram/releases/latest) into project
          root.
        - Run `java -jar ./ChartGram*.jar` from project root.

    - #### From source
        - Run `./gradlew run` from project root.

    - #### Build from sources and run from generated artifact
        - Run `./gradlew build` from project root.
        - Run `java -jar ./build/libs/ChartGram*.jar` from project root.