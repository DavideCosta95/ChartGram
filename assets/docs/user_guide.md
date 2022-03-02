# Configuration files

ChartGram uses four configuration files:

- `application.json`
    - logging configuration file path
    - webapp and REST API connection port
    - database jdbc url
    - hibernate default schema
- `configuration.json`
    - test flag to enable [test mode](/assets/docs/user_guide.md#test-mode)
    - language used for template messages (must be present an entry key with the same name in `localization.json`)
    - preferred timezone in a compatible format (
      see [documentation](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/TimeZone.html#getTimeZone(java.lang.String)))
    - bot enabled flag to disable bot functionalities (i.e. if one has not a bot API token)
    - bot's name, username and token
      from [Telegram bot setup process](/assets/docs/advanced_setup.md#telegram-bot-setup-telegram-account-needed) (
      ignored if `bot.enabled` is `false`)
    - string mappings for bot commands
    - ignore non commands messages flag to avoid sending responses to non-command messages from users
    - developers id list for bot startup message and error reporting
    - base url for domain customization to access webapp and API via generated urls
- `localization.json`
    - localization strings used in Telegram bot messages templates
- `logback.xml`
    - logging configuration

All the properties contained in such configuration files are NOT hot swappable, you need to relaunch the application
before changes take effect.

# Telegram groups features usage

TODO

# API

The application exposes a REST API to access collected data.
By default, an authentication is needed to perform requests to such API.
To disable authentication for API use, set `"test": false` in `./config/configuration.json`, with reference to [test mode](/assets/docs/user_guide.md#test-mode).

## Endpoints

The following endpoints are served from the base path (i.e. by default `localhost:8080`) and all return JSON arrays of object, with respect to the respective endpoint purpose:

- `/api/groups/{groupId}/users`
  - example response:
  ```
  [
    {
      "id": 1,
      "telegramId": "10000000",
      "telegramFirstName": "first_name_1",
      "telegramLastName": "last_name_1",
      "telegramUsername": "username_1",
      "insertedAt": "2022-02-21T22:24:58.637772"
    }
  ]
  ```

- `/api/groups/{groupId}/messages`
  - example response:
  ```
  [
    {
      "id": 1,
      "sentAt": "2022-02-21T22:24:58.637772",
      "sender": {
        "id": 1,
        "telegramId": "10000000",
        "telegramFirstName": "first_name_1",
        "telegramLastName": "last_name_1",
        "telegramUsername": "username_1",
        "insertedAt": "2022-02-21T22:24:58.637772"
      },
      "group": {
        "id": 1,
        "telegramId": "-100000000000",
        "description": null,
        "insertedAt": "2022-02-21T22:24:58.637772"
      },
      "text": "text",
      "type": 1
    }
  ]
  ```
- `/api/groups/{groupId}/join-events`
  - example response:
  ```
  [
    {
      "id": 1,
      "joinedAt": "2022-02-22T16:57:28.263985",
      "joiningUser": {
        "id": 1,
        "telegramId": "10000000",
        "telegramFirstName": "first_name_1",
        "telegramLastName": "last_name_1",
        "telegramUsername": "username_1",
        "insertedAt": "2022-02-21T23:22:14.029409"
      },
      "adderUser": {
        "id": 2,
        "telegramId": "20000000",
        "telegramFirstName": "first_name_2",
        "telegramLastName": "last_name_2",
        "telegramUsername": "username_2",
        "insertedAt": "2022-02-21T22:24:58.637772"
      }
    }
  ]
  ```
- `/api/groups/{groupId}/leave-events`
  - example response:
  ```
  [
    {
      "id": 1,
      "leavingAt": "2022-02-22T16:58:09.436446",
      "leavingUser": {
        "id": 1,
        "telegramId": "10000000",
        "telegramFirstName": "first_name_1",
        "telegramLastName": "last_name_1",
        "telegramUsername": "username_1",
        "insertedAt": "2022-02-21T22:24:58.637772"
      },
      "removerUser": {
        "id": 2,
        "telegramId": "20000000",
        "telegramFirstName": "first_name_2",
        "telegramLastName": "last_name_2",
        "telegramUsername": "username_2",
        "insertedAt": "2022-02-21T23:22:14.029409"
      }
    }
  ]
  ```

The `{groupId}` path variable represents the Telegram id of the group which is querying for.
It must match the group id mapped by the authorization token of the bearer, more details in the [authentication section](/assets/docs/user_guide.md#authentication).

## Authentication

To be able to perform API calls (except for [test mode](/assets/docs/user_guide.md#test-mode)), it's mandatory to present an authorization token.
This can be done in two equivalent ways:
- Adding it as a request header: `authorization: <token>`.
- Making the webapp set a session cookie when landing from the bot-generated link.

For this base version of the application, the only way to obtain an authorization token is to use the `analytics_command` from the bot.
The aforesaid token will be added as `authorization` query param in the generated url.
An example of said url is the following: `http://localhost:8080/webapp/groups/-1001338226930/?authorization=31ff6c8e-6373-4324-a810-5c10f9cc28a9`

# Test mode

Setting `"test": false` in `./config/configuration.json` will produce two effects:
- API will not ask for an [authorization token](/assets/docs/user_guide.md#api) to retrieve data.
- Using `analytics_command` (default=`/analytics`) and `charts_command` (default=`/charts`), both in `./config/configuration.json`, in a private chat with the bot would normally lead to an error message but in this mode the bot will send to the user respectively:
  - `analytics_command` a link to the webapp to view an example group data (the data of the group with the lowest id in the database, technically).
  - `charts_command` all the implemented charts, representing an example group data, like stated in the previous point.
  
In this mode, one will be able to see an arbitrary group data via API and using bot's commands, changing at will the group id in the url.

# Assets

## Database E/R diagram:

![DB E/R schema](/assets/images/db_er_schema.png)

## Example screens

TODO