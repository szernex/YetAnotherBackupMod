# YetAnotherBackupMod (YABM)
A Minecraft Forge 1.7.10 Mod for automatically creating backups of your saves and other stuff.

## Features
* automatic scheduled backups or
* interval based backups
* configurable via config file, mod configuration screen or in game via commands
* fully customizable for the most parts; decide what to backup and where to store it, etc.
* automatic backup consolidation (only keep max X backups) to save space
* persistent backups for each day (excluded from automatic consolidation)

## Usage
After installation players (in SP)/OPs (on servers) can use the /yabm command to configure settings and manually start backups.

Examples:
* /yabm get backupSchedule - returns the schedule times for when to automatically create backups. Currently global for all saves.
* /yabm set backupSchedule 6:00 12:00 18:00 0:00 - sets the times for when to automatically create backups to the given times.
* /yabm set backupSchedule 180 - sets it to make backups every 3 hours.
* /yabm startbackup - manually starts the backup process.
