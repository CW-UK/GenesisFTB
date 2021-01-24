## General
- Uses WorldGuard region flag ('ftb-zone') to define arenas.
- Live logging of buttons when added/removed before game start.
- Optional public broadcast when buttons are placed or removed before game starts.
- Public announcement when a button is found, including buttons remaining.
- Automatic end-of-game announcement when all buttons found.
- Permission genesisftb.admin  to access and use commands
- Staff with above permission cannot accidentally click or break buttons before a game has started.
- Game cannot start until at least one button has been placed.
- Only includes buttons that are placed pre-game. Existing buttons like decoration are not part of the game.

## Commands (everyone)
- `/ftb` - Shows your win count.
- `/ftb <player>` - Shows win count of that player.

## Commands (permission genesisftb.admin):
- `ftb start` - Starts a game providing more than 1 button has been placed.
- `/ftb list` - Lists the locations of buttons that haven't been found yet.
- `/ftb reset` - Resets the game and removes any placed buttons, also cancels any in-progress game.
- `/ftb found` - Lists the buttons and who found them either in the current game or in the previous game.
- `/ftb count` - Prints a list of players and the total number of buttons they found in the current or previous game.
- `/ftb broadcast <message>` - Broadcasts a message with the FTB prefix. Supports standard &X colours.
- `/ftb opendoors <main|game>` - Opens all doors that have been added.
- `/ftb closedoors <main|game>` - Closes all doors that have been added.

## Commands (OP required)
- `/ftb cleardatabase <code>` - Resets the database. Use without code first to obtain reset code.
- `/ftb setscore <name> <new_score>` Sets the score for that player, or removes from database if 0.
- `/ftb tool` - Gives you the tool used to add/remove doors. (requires OP to use tool)
- `/ftb reload` - Reloads the plugin config file.
- `/ftb toggle` - Enables/disables the plugin so that decorative buttons can be placed.
