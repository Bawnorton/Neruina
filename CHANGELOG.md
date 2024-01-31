# 2.0.0
### Changes
- Added NeoForge support
- Added new actions to the ticking entity broadcast
  - What Is This?: Opens the Neruina wiki page 
  - Open Log: Opens your latest.log file
  - Copy Crash: Copies the cause of the ticking exception to your clipboard
- Improved performance and memory usage by delegating the errored state to the ticking entity
- Migrated to Stonecutter to ease multi-loader multi-version development

### Config
- New `log_level` option that replaces `broadcast_errors`
  - `operators` (default) - Only operators will receive the broadcast
  - `everyone` - Everyone will receive the broadcast
  - `disabled` - No one will receive the broadcast


### Fixes
- Fixed a crash with newer versions of Forge
