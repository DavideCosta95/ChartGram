#ChartGram
## Description

TODO

## Setup

### 1) Configuration files

#### 1.1

- Clone this repository and checkout the `master` branch to be on the latest stable version.

#### 1.2

- Create a new `application.json` file in the `./config` sub-folder and paste all the content
  of `./config/application.json.example` into it.

#### 1.3

- Create a new `configuration.json` file in the `./config` sub-folder and paste all the content
  of `./config/configuration.json.example` into it.

### 2) Database setup (one of the following)

#### 2.1a - Using docker-compose

- Run `docker-compose up -d` from the project root to run a local ready-to-use PostgreSQL DBMS instance, with also
  pre-loaded example data.

#### 2.1b - Using a custom RDBMS instance

- Edit `./config/application.json` customizing the `spring.datasource.url` property to point to your RDBMS instance.

##### N.B.: The following two scripts are written for PostgreSQL DBs, you may need to adapt them to your RDBMS SQL dialect.

- Run `./src/sql/create.sql` script to set up your database for the application.
- Run `./src/sql/example_data.sql` to load an example dataset into your database.

### 3) Telegram bot setup (optional, Telegram account needed)

- Open [BotFather's chat](https://t.me/botfather).
- Create a new bot using `/newbot` command.
- Copy the API token returned by BotFather at the end of the bot creation process and paste it to the `bot.token` property in `./config/configuration.json`.
- In `./config/configuration.json` set `bot.enabled` property to `true`.

### 4) API

- To enable authentication for API use, set `"test": false` in `./config/configuration.json`.

## Usage

- ### Using distributable jar
    - Download the [latest stable release](https://github.com/DavideCosta95/ChartGram/releases/latest) in project root.
    - Run `java -jar ./ChartGram*.jar` from project root.
- ### From sources
    - Run `gradle build` from project root.
    - Run `java -jar ./build/libs/ChartGram*.jar` from project root.

- ### To just run the application
    - Run `gradle run` from project root.

## Database E/R diagram:

![DB E/R schema](/assets/images/db_er_schema.png)