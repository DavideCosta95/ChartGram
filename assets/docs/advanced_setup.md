# Using a custom RDBMS instance

- Edit `./config/application.json` customizing the `spring.datasource.url` property to point to your RDBMS instance.

#### N.B.: The following two scripts are written for PostgreSQL DBs, you may need to adapt them to your RDBMS SQL dialect.

- Run `./src/sql/create.sql` script to set up your database for the application.
- Run `./src/sql/example_data.sql` to load an example dataset into your database.

# Telegram bot setup (Telegram account needed)

- Open [BotFather's chat](https://t.me/botfather) on Telegram.
- Create a new bot using `/newbot` command.
- Copy the API token returned by BotFather at the end of the bot creation process and paste it into the `bot.token` property in `./config/configuration.json`.
- Edit `./config/configuration.json` setting `bot.enabled` property to `true`.