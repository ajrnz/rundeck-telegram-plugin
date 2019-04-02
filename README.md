
Rundeck Telegram Plugin   ![Build Status](https://travis-ci.org/ajrnz/rundeck-telegram-plugin.svg)
=======================


Sends Rundeck notifications to [Telegram](http://www.telegram.org) a mobile and desktop messaging system.


Installation
------------

1. Download from GitHub ([`rundeck-telegram-plugin-<version>.jar`](https://github.com/ajrnz/rundeck-telegram-plugin/releases/latest)) or build from source
2. Copy the plugin jar (`rundeck-telegram-plugin-<version>.jar`) to `<Rundeck>/libext` directory. It will be picked up and installed instantly - a restart is not required.


Building
--------
The plugin is written in scala so you need to have [mill](http://www.lihaoyi.com/mill/) installed.
Build the plugin:

    mill plugin.assembly

The plugin will be placed in `out/plugin/assembly/dest/out.jar`

it should be renamed `rundeck-telegram-plugin-<version>.jar` and placed in the `libext` directory of your rundeck instance.


Configuration
-------------

### Telegram configuration

You need to set up a [Telegram Bot](https://core.telegram.org/bots) to send the message from. Have a chat with the Botfather to do this. Next you need to get the IDs of the chats that you want to send messages to. The only way to find these is to have them send a message to you bot and pick them out of the JSON. You can do this via

    curl "https://api.telegram.org/bot<not-auth-token>/getUpdates"

You can them map your bots and chats to aliases to use in the plugin. Eg put the following in `/etc/rundeck/telegram.properties`

    telegram.ids.bot.messenger_bot=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    telegram.ids.chat.devops-alerts=-123456789
    telegram.ids.chat.news=-987654321
    telegram.ids.chat.alice=11111111
    telegram.ids.chat.bob=22222222

You can then refer to your bots and chats by name in the plugin although the IDs are accepted as well.


### Plugin configuration

Got to `Configure -> List Plugins -> Notification Plugins -> Telegram` to see a list of the configuration options. 

You will need to generate a [Rundeck API key](http://rundeck.org/2.6.7/api/index.html#token-authentication
) if you want to include the job log in the message as the plugin retrieves the log via the Rundeck API.

Settings can be edited in the GUI from `Configure -> Project Configuration: <Project> -> Edit Configuration File`

    project.plugin.Notification.TelegramNotification.projectBotAuthToken=messenger_bot
    project.plugin.Notification.TelegramNotification.rundeckApiKey=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX


Message Templates
-----------------

The plugin uses [Freemarker](http://www.freemarker.org) to render templates. Freemarker is quite an extensive templating language and should be suitable for most needs. Basic substitutions can be done using the `${var}` syntax for example:

    ${job.project}: ${job.group}/${job.name}

could be used to output the project, group and job name. 

Templates can come from three sources with the first having priority

1. The message template string in the job definition
2. The message template name in the job definition
3. The message template name in the project or framework property

The location from which the templates are loaded can be controlled with the `project.plugin.Notification.TelegramNotification.templateDir` configuration setting

For example you might make a template file `status.ftl`:

    ${job.project}: ${job.group}/${job.name}
    Status: ${status} (${context.job.execid})


For reference the available variables for substitution look like the following:

    {
      id=60,
      href=http://192.168.0.34:4440/project/Production/execution/follow/60,
      status=succeeded,
      user=admin,
      dateStarted=2016-01-05 21:32:16.879,
      dateStartedUnixtime=1452029536879,
      dateStartedW3c=2016-01-05T21:32:16Z,
      description=,
      argstring=null,
      project=Production,
      failedNodeListString=null,
      failedNodeList=null,
      succeededNodeListString=jack,
      succeededNodeList=[jack],
      loglevel=INFO,
      dateEnded=2016-01-05 21:32:17.462,
      dateEndedUnixtime=1452029537462,
      dateEndedW3c=2016-01-05T21:32:17Z,
      abortedby=null,
      nodestatus={
        succeeded=1,
        failed=0,
        total=1
      },
      job={
        id=a04200e8-2498-4b0a-9a56-71b731e1f780,
        href=http://192.168.0.34:4440/project/Production/job/show/a04200e8-2498-4b0a-9a56-71b731e1f780,
        name=telegram_job,
        group=test,
        project=Production,
        description=,
        averageDuration=2114
      },
      context={
        job={
          wasRetry=false,
          user.name=admin,
          project=Production,
          url=http://192.168.0.34:4440/project/Production/execution/follow/60,
          execid=60,
          serverUUID=null,
          serverUrl=http://192.168.0.34:4440/,
          loglevel=INFO,
          name=telegram_job,
          id=a04200e8-2498-4b0a-9a56-71b731e1f780,
          retryAttempt=0,
          group=test,
          username=admin
        },
        option={}
      }
    }


Troubleshooting
---------------

If messages are not being send take a look in `/var/log/rundeck/service.log` for any hints


Change Log
----------

1.1.0
-----
- Moved build to mill


1.0.7
-----
- Tweaks to work with RunDeck 3 (thanks @NFDWADM)
- use `dateStartedIsoString` and `dateEndedIsoString` to get string dates

...