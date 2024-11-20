# SimpleChat (fork++)

![Modrinth downloads badge](https://img.shields.io/modrinth/dt/PXaxqCH3)
![Modrinth versions badge](https://img.shields.io/modrinth/game-versions/PXaxqCH3)
![java 17 badge](https://img.shields.io/badge/java-21+-orange?logo=java)

### This fork was created because the main project is [no longer maintained](https://github.com/cayennemc/SimpleChat/issues/11).

### Log: "This is a fork of the SimpleChat mod by [rantuhin](https://github.com/cayennemc/SimpleChat) and its fork by [stanlystark](https://github.com/stanlystark/SimpleChat) for 1.21"

_A simple chat mod for your server._

Doesn't work in single player.

Just use `!<message>` for global chat or `#<message>` for world chat!

![Example image showing formatted chat messages](https://i.imgur.com/aWeZ1DV.png)
## Features
- FTB Teams integration _(no tested)_
- LuckPerms integration _(tested 5.4)_
- Vanish integration _(tested 1.5.7)_
- Global, world and local chat (you can turn it off)
- Color chat (you can turn it off)
- Reloading the configuration with the command

## Configuration
The configuration is located in `<game or server directory>/config/simplechat.json`.
| Name | Description | Type |
|-|-|-|
| enable_chat_mod | Enables (true) or disables (false) chat handling by the mod. | boolean |
| enable_global_chat | Enables (true) or disables (false) the global chat. | boolean |
| enable_world_chat | Enables (true) or disables (false) the world chat. | boolean |
| enable_chat_colors | Enables (true) or disables (false) the use of color codes in the chat. | boolean |
| local_chat_format | Defines the appearance of the local chat. | String |
| global_chat_format | Defines the appearance of the global chat. | String |
| no_players_nearby_text | Defines a message for local chat when there are no players nearby. | String |
| no_players_nearby_action_bar | Enables (true) or disables (false) action bar message. | boolean |
| chat_range | Specifies the distance after which local chat messages will not be visible (if global chat is enabled). | int |

```json
{
  "enable_chat_mod": true,
  "enable_global_chat": true,
  "enable_world_chat": false,
  "enable_chat_colors": false,
  "local_chat_format": "&7%ftbteam%&r%lp_prefix%&r%player%&7:&r &7%message%",
  "global_chat_format": "&8[&2G&8] &7%ftbteam%&r%lp_prefix%&r%player%&7:&r &e%message%",
  "world_chat_format": "&8[&9W&8] &7%ftbteam%&r%lp_prefix%&r%player%&7:&r &e%message%",
  "no_players_nearby_text": "&fNo players nearby. Please use &e!<message> &ffor global chat.",
  "no_players_nearby_action_bar": false,
  "chat_range": 100
}
```
You can use the placeholder `%player%` to specify the player's nickname and the placeholder `%message%` to specify their message in the chat.

- `%ftbteam%` FTB Team integration - display your party in chat.
- `%lp_group%` LuckPerms - display player group.
- `%lp_prefix%` LuckPerms - display player prefix.
- `%lp_suffix%` LuckPerms - display player suffix.

You can reload the configuration without restarting the server or the game using the `/simplechat` command (requires [permission level](https://minecraft.fandom.com/wiki/Server.properties#op-permission-level) 1 or more).

## NO API
I am removing `me.vetustus.server.simplechat.api.event.PlayerChatCallback` as I didn't see the need for it, and instead of porting it to 1.21, I am using the standard event `net.fabricmc.fabric.api.message.v1.ServerMessageEvents` `ALLOW_CHAT_MESSAGE`.

## License
The MIT license is used.