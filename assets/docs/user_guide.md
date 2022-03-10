# Test mode

To enable **test mode** set `"test": true` in `./config/configuration.json`.  
It will produce two effects:
- API will not ask for an [authorization token](/assets/docs/user_guide.md#authentication) to retrieve data.
- Using `analytics_command` (default=`/analytics`) and `charts_command` (default=`/charts`), both in `./config/configuration.json`, in a private chat with the bot would normally lead to an error message but in this mode the bot will send to the user respectively:
  - `analytics_command` a link to the webapp to view an example group data (the data of the group with the lowest id in the database, technically).
  - `charts_command` all the implemented charts, representing an example group data, like stated in the previous point ([generated charts examples](/assets/docs/example_screens.md#example-chart-images-rendered-by-bot))

In this mode, one will be able to see an arbitrary group data via API and using bot's commands, changing at will the group id in the url.

# Telegram groups features usage

In order to collect data and generate relative charts:
- Perform the [Telegram bot setup](/assets/docs/advanced_setup.md#telegram-bot-setup-telegram-account-needed).
- Press the "Start" button in the brand-new bot chat.
- Add the bot to the desired group.
- Promote it to administrator (needed to see all messages and events in the group).
- [Run the application](/README.md#usage).

The bot will store information about people joining/leaving the group and about messages sent into it, according to the [data model](/assets/docs/user_guide.md#database-er-diagram).  
To retrieve this data, three ways are available, with respective returned data format:
- Through [API](/assets/docs/user_guide.md#api)
  - JSON array
- Using bot's `analytics_command`
  - url to browse data on webapp
- Using bot's `charts_command`
  - rendered charts images sent via Telegram

Bot commands must be used in the group which one is interested in.

#### N.B.: With [test mode](/assets/docs/user_guide.md#test-mode) disabled, only group administrators are able to access their group's data.

Bot interactions example screens available [here](/assets/docs/example_screens.md).

# Webapp
Collected data is available also on a webapp, reachable at `http://<BASE_URL>:<PORT>/webapp/groups/{groupId}/?authorization={token}`.  
To configure `BASE_URL` and `PORT` in said url, go to [configuration files](/assets/docs/user_guide.md#configuration-files) guide section.   
The `{groupId}` path variable represents the Telegram id of the group which is querying for.  
It must match the group id mapped by the authorization `token` present as query param, more details in the [authentication section](/assets/docs/user_guide.md#authentication).  

The webapp will show various charts and information from the group ([example screen](/assets/docs/example_screens.md#webapp)).  
It will also display the authenticated user's Telegram profile picture and name on the right of the top bar.  

About the security layer, it is demanded to a reverse-proxy, to be placed architecturally between the application and the extern.  
In this way, the system is more modular, and it's possible to restart this entry point without impacting the application, other than being able to change cipher key and more various tunings.  

# API

The application exposes a REST API to access collected data.  
By default, an authentication is needed to perform requests to such API.  
To disable authentication for API use, set `"test": true` in `./config/configuration.json`, with reference to [test mode](/assets/docs/user_guide.md#test-mode).  

## Endpoints

The following endpoints are served from the base path (i.e. by default `localhost:8080`) and all return JSON arrays of object, with respect to the respective endpoint purpose.  
All the calls must be performed by `HTTP GET` requests.  

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
        "title": "title_1",
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
      },
      "group": {
        "id": 1,
        "telegramId": "-100000000000",
        "title": "title_1",
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
      },
      "group": {
        "id": 1,
        "telegramId": "-100000000000",
        "title": "title_1",
        "insertedAt": "2022-02-21T22:24:58.637772"
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

For this base version of the application, the only way to obtain an authorization token is to use the bot's `analytics_command`.  
The aforesaid token will be added as `authorization` query param in the generated url.  
An example of said url is the following:  
`http://localhost:8080/webapp/groups/-1001338226930/?authorization=31ff6c8e-6373-4324-a810-5c10f9cc28a9`  

## Database E/R diagram:

![DB E/R schema](/assets/images/db_er_schema.png)

# Configuration files

ChartGram uses four configuration files:

- `application.json`
  - Logging configuration file path.
  - Webapp and REST API connection port.
  - Database jdbc url.
  - Hibernate default schema.
- `configuration.json`
  - Test flag to enable [test mode](/assets/docs/user_guide.md#test-mode).
  - Language used for template messages (must be present an entry key with the same name in `localization.json`).
  - Preferred timezone in a compatible format (see [documentation](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/TimeZone.html#getTimeZone(java.lang.String))).
  - Bot enabled flag to disable bot functionalities (i.e. if one has not a bot API token).
  - Bot's name, username and token from [Telegram bot setup process](/assets/docs/advanced_setup.md#telegram-bot-setup-telegram-account-needed) (ignored if `bot.enabled` is `false`).
  - String mappings for bot commands.
  - Ignore non commands messages flag to avoid sending responses to non-command messages from users.
  - Developers id list for bot startup message and error reporting.
  - Base url for domain customization to access webapp and API via generated urls.
- `localization.json`
  - Localization strings used in bot's messages templates.
- `logback.xml`
  - Logging configuration.

All the properties contained in such configuration files are NOT hot swappable, you need to relaunch the application
before changes take effect.