# Configuration files

Application uses four configuration files:

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
    - bot enabled flag to disable bot functionalities (i.e. if you have not a bot API token)
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

TODO

# Database E/R diagram:

![DB E/R schema](/assets/images/db_er_schema.png)