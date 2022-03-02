# ChartGram

## Description

TODO

## Usage

- ### Database setup
    - Run `docker-compose up -d` from the project root to run a local ready-to-use PostgreSQL DBMS instance, with also
      pre-loaded example data.
    - To use a custom RDBMS instance see [advanced configuration](/assets/docs/advanced_configuration.md).

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

# [User guide](/assets/docs/user_guide.md)