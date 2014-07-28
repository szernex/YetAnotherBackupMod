# YetAnotherBackupMod (YABM)
A Minecraft Forge 1.7.10 Mod for automatically creating backups of your saves and other stuff.

## Usage
After installation players (in SP)/OPs (on servers) can use the /yabm command to configure settings and manually start backups.

Examples:
/yabm get backupTimes - returns the schedule times for when to automatically create backups. Currently global for all saves.
/yabm set backupTimes 6:00 12:00 18:00 0:00 - sets the times for when to automatically create backups to the given times.
/yabm startbackup - manually starts the backup process.