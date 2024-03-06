# 2.0.0
### Changes
- Added NeoForge support
- New ticking suspension system:
  - Entities, block entities, block states and item stacks will no longer be killed or removed but will instead be 
    suspended until an operator takes action.
  - This should mitigate the undesired outcome where an important or useful entity is killed.
- Added new actions to the ticking entity broadcast
  - `What Is This?`: Opens the Neruina wiki page on what Neruina is and what it does
  - `Copy Crash`: Copies the cause of the ticking exception to your clipboard
  - `Report`: Opens a new issue on the [NeruinaAutoReports](https://github.com/Bawnorton/NeruinaAutoReports) GitHub 
    repository and any mods that opt-in to the reporting system
  - `Teleport`: Teleports you to the location of the ticking entity
  - `Try Resume`: Attempts to resume the ticking of the ticking entity
  - `Kill`: Immediately kills and removes the ticking entity
- Persitance:
  - Ticking entity tracking will now persist across server restarts 
  - When the world is first started Neruina will broadcast all tracked ticking entities that need addressing
- Ticking Threshold:
  - When a certain number of ticking excpetions occur within a certain time frame, Neruina will deliberately crash in 
    order to prevent the server from becoming unusable.
  - A comprehensive report will be generated with every ticking exception that occurred with instructions on what to do next.
  - The default threshold is 20 exceptions within 5 minutes, this can be changed in the config.
- Improved performance and memory usage by delegating the ticking state to the ticking entity
- Migrated to Stonecutter to ease multi-loader multi-version development

### Auto Reports
- Added a new system for mods to opt-in to the auto reporting system
- See the schema [here](https://github.com/Bawnorton/Neruina/wiki/Auto-Report-Schema)

### Commands
- These are designed to be used internally by Neruina as it is a server-side mod and allows the client messages to 
  interact with the mod, but they are available to operators as well.
- `/neruina resume <entity|pos|player>`
  - `entity`: Resumes the ticking of the specified entity
  - `pos`: Resumes the ticking of the block entity or block state at the specified position
  - `player`: Resumes the ticking of the held item of the specified player
- `/neruina kill <entity>`: Immediately kills and removes the specified entity
- `/neruina id <entity|pos>`
  - `entity`: Returns the report UUID of the specified entity if it is being tracked 
  - `pos`: Returns the report UUID of the block entity at the specified position if it is being tracked
- `/neruina info <uuid>`: Sends the error message of the specified report UUID to the player
- `/neruina report <uuid>`: Automatically generates a report for the specified ticking entity via it's tracked report 
  UUID

### Config
- New `log_level` option that replaces `broadcast_errors`
  - `operators` (default) - Only operators will receive the broadcast
  - `everyone` - Everyone will receive the broadcast
  - `disabled` - No one will receive the broadcast
- New `ticking_exception_threshold`
  - The number of ticking exceptions that can occur within the specified time frame before Neruina will deliberately 
    crash
  - Default is `20`
  - `-1` will disable the threshold
- New `auto_kill_ticking_entities`
  - If true, ticking entities will be immediately killed and removed rather than suspended
  - Default is `false`

### Fixes
- Fixed a crash with newer versions of Forge
- Fixed a crash with does potato tick 1.18.2
