# Neruina

### This is a mod that prevents ticking crashes from bricking worlds.

[![Modrinth](https://img.shields.io/modrinth/dt/neruina?colour=00AF5C&label=downloads&logo=modrinth)](https://modrinth.com/mod/neruina)
[![CurseForge](https://cf.way2muchnoise.eu/full_851046_downloads.svg)](https://curseforge.com/minecraft/mc-mods/neruina)

### When an Entity, Block or Item causes a ticking crash:
- That Entity will be suspended and no longer tick, you can still interact with the entity but cannot attack it.
- That Block Entity / Tile Entity or Block State (for random ticks) will no longer tick, you can still access the block inventory if it has one.
- That Item will stop ticking in the inventory but will still persist and function in recipes and most uses, nothing is lost.
- If another mod causes the Player to crash on tick, the Player will be kicked instead.

### Actions:
- `What Is This?`: Opens the Neruina wiki page on what Neruina is and what it does
- `Copy Crash`: Copies the cause of the ticking exception to your clipboard
- `Teleport`: Teleports you to the location of the ticking entity
- `Try Resume`: Attempts to resume the ticking of the ticking entity
- `Kill`: Immediately kills and removes the ticking entity
- `Report` (1.19+): Opens a new issue on the [NeruinaAutoReports](https://github.com/Bawnorton/NeruinaAutoReports)
  GitHub repository and any mods that opt-in to the reporting system

### Persitance:
- Ticking entity tracking will now persist across server restarts
- When the world is first started Neruina will broadcast namespaces tracked ticking entities that need addressing

### Ticking Threshold:
- When a certain number of ticking excpetions occur within a certain time frame, Neruina will deliberately crash in
  order to prevent the server from becoming unusable.
- A comprehensive report will be generated with every ticking exception that occurred with instructions on what to do next.
- The default threshold is 10 exceptions within 5 minutes, this can be changed in the config.

### Config
- `min_permission_level_for_messages`
    - The minimum permission level a player must have to recieve neruina broadcasts
    - Default is `0`
- `min_permission_level_for_commands`
    - The minimum permission level a player must have to run neruina actions (commands)
    - Default is `2` 
- `ticking_exception_threshold`
    - The number of ticking exceptions that can occur within the specified time frame before Neruina will deliberately
      crash
    - Default is `10`
    - `-1` will disable the threshold
- `auto_kill_ticking_entities`
    - If true, ticking entities will be immediately killed and removed rather than suspended
    - Default is `false`
- `handle_ticking_<type>`
    - If false, ticking <type> will not be handled and neruina will allow the game to crash
    - Default is `true`   
