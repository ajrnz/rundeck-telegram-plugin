
Rundeck Telegram Plugin
=======================

Sends Rundeck notifications to [Telegram](http://www.telegram.org) a mobile and desktop messaging system.

The current version is 1.0.0.


Installation
------------

1. Download from Maven Central ([rundeck-telegram-plugin](http://search.maven.org/#search%7Cga%7C1%7Crundeck-telegram-plugin) or build from source
2. Copy the plugin jar (rundeck-telegram-plugin-\<version\>.jar) to \<Rundeck\>/libext directory. It will be picked up and installed instantly - a restart is not required.


Building
--------
The plugin is written in scala so you need to have [SBT](http://www.scala-sbt.org/) installed.
Build the plugin:

    sbt rundeckPlugin

The plugin will be placed in `target/scala-2.11/`


Configuration
-------------

You need to set up a [Telegram Bot](https://core.telegram.org/bots) to send the message from. Have a chat with the Botfather to do this. 




For reference the available variables look like the following:

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

