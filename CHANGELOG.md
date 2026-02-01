# Changelog

## 2.4.4
- Fixed compatibility with Minecraft 1.21.x (NoSuchMethodError: getServer())

## 2.4.3
- Removed FTB Ranks/Teams integration
- Fixed chat formatting colors being lost when click event is added to player name
- Fixed false "no players nearby" notification when Vanish mod is not installed
- Fixed UUID and world comparison issues
- Fixed potential regex injection in player names and messages
- Fixed memory leak when reading config file

- Add command `/simplechat clear` - clear chat (all players)
- Add `"suggests_when_you_click_on_the_player_name": "/msg %player% "`
