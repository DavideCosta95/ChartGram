# ChartGram

## Description

ChartGram is a Telegram bot implementation, which collects data from the groups it's added as administrator and generates charts and graphs for these groups' administrators.  
It also offers an authentication-based API to serve this data to external systems.  
ChartGram consists in:
- #### Telegram bot
  - Collects data in order to generate rendered images of charts and graphs and gives authorization tokens to view analytics on the webapp.
- #### Webapp
  - Offers various charts and graphs about a given group, retrieving data from the API.
- #### REST API
  - Serves authenticated requests over HTTP, using JSON-serialized objects arrays.

More details, about the implemented use-cases, available in the [user guide](/assets/docs/user_guide.md).  
To test the application with the pre-loaded example data, follow the [test mode](/assets/docs/user_guide.md#test-mode) section of the guide.  
Example screens of the application available [here](/assets/docs/example_screens.md).  
New charts and graphs are easily implementable at will, being the architecture flexible and modular.  

## Usage

- ### Database setup
    - Run `docker-compose up -d` from the project root to run a local ready-to-use PostgreSQL DBMS instance, with also
      pre-loaded example data.
    - To use a custom RDBMS instance see [advanced setup](/assets/docs/advanced_setup.md).

- ### Run
    - #### From distributable jar (recommended way)
        - Download the [latest stable release](https://github.com/DavideCosta95/ChartGram/releases/latest) into project
          root.
        - Run `java -jar ./ChartGram-<VERSION>.jar` from project root.

    - #### From source
        - Run `./gradlew run` from project root.

    - #### Build from sources and run from generated artifact
        - Run `./gradlew build` from project root.
        - Run `java -jar ./build/libs/ChartGram-<VERSION>.jar` from project root.

After the first installation, the application is already in [test mode](/assets/docs/user_guide.md#test-mode).  
Can be tested going to the webapp default url `http://localhost:8080/webapp/groups/1` to view the example group data, or using [bot commands](/assets/docs/user_guide.md#telegram-groups-features-usage).

## Public instance

An up and running public instance is available for free [here](https://t.me/ChartGramBot).  
It's hosted on `chartgram.ddns.net`, with [test mode](/assets/docs/user_guide.md#test-mode) disabled.