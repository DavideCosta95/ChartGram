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

# API

The application exposes a REST API to access collected data.
By default, an authentication is needed to perform requests to such API.
To disable authentication for API use, set `"test": false` in `./config/configuration.json`.

# Test mode

Setting `"test": false` in `./config/configuration.json` will produce two effects:
- API will not ask for an [authorization token](/assets/docs/user_guide.md#api) to retrieve data.
- Using `analytics_command` (default=`/analytics`) and `charts_command` (default=`/charts`), both in `./config/configuration.json`, in a private chat with the bot would normally lead to an error message but in this mode the bot will send to the user respectively:
  - `analytics_command` a link to the webapp to view an example group data (the data of the group with the lowest id in the database, technically).
  - `charts_command` all the implemented charts, representing an example group data, like stated in the previous point


In this mode, one will be able to see an arbitrary group data via API and using bot's commands, changing at will the group id in the url.

# Database E/R diagram:

![DB E/R schema](/assets/images/db_er_schema.png)