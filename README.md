[![Download](https://api.bintray.com/packages/spacecookie/generic/modularbot/images/download.svg)](https://bintray.com/spacecookie/generic/modularbot/_latestVersion)
[![JDA Version](https://img.shields.io/badge/JDA-3.3.1__303-brightgreen.svg)](https://github.com/DV8FromTheWorld/JDA)
[![LavaPlayer Version](https://img.shields.io/badge/LavaPlayer-1.2.44-brightgreen.svg)](https://github.com/sedmelluq/lavaplayer)

# ModularBot framework
ModularBot is a java framework designed to help you creating a discord bot using Java without the boring parts.
The "Modular" is not just a random word, the framework come with some default managers but they are all overridable from the logging system to the handling of commands for more flexibility.

## Installation
You can install the framework through gradle with this:
```gradle
repositories {
    jcenter()
    maven {
        url 'https://dl.bintray.com/spacecookie/generic'
    }
}

dependencies {
    compile 'com.jesus_crie:modularbot:1.3.1'
}
```

## Features
- Sharding support.
- Command parsing using regex (can parse int, user, channel, emotes, url, email, ...).
- Customizable command handling (middleware & errors) (context, guild only, ...)
- Config support.
- Config loading/saving customizable.
- Log output customizable.
- Global stats.
- Templates (embeds & plain text).
- Decorators to attach to messages (easy dismissible messages / dialog boxes / polls / panels) (WIP)

## Getting started
Click [here](https://github.com/JesusCrie/ModularBot/wiki) to access the documentation.