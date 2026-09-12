# Purrsuit

Purrsuuit is an Arcade-puzzler built with libGdx. Control a speedy cat while protecting a tether block of cheese that you stole from a colony of hungry mice.
Toggle doors with switches, collect coins, and pickup catnip that drop from mice for a power up.
Currently only 3 levels are playable.

## Controls

- W/A/S/D - move
- Space - shoot yarn
- F1 or ~ (tilde) - toggle cheat console
- Mouse - UI only

## START COPILOT EDIT
## Build a Windows executable (.exe)

From the repository root, run:

`./gradlew lwjgl3:packageWinX64`

On Windows command prompt/PowerShell, run:

`gradlew.bat lwjgl3:packageWinX64`

The Windows package is generated under:

`lwjgl3/build/construo/winX64/`

Open `lwjgl3/build/construo/winX64/roast/Purrsuit.exe`.

Important: the launcher is not a standalone `.exe`. Keep `Purrsuit.exe`, `Purrsuit-1.0.0.jar`, the `app/` folder, and the `runtime/` folder together or the game will not start.
## END COPILOT EDIT

## Cheat Console Commands
- 'help' - list all commands
- 'god' - cheese becomes invulnernable to all damage
- 'level -<n> - jump to level n (ex. 'level -3' jumps to level 3)
- 'give catnip' - spawn a catnip power up 1 tile in front of the player if the space is open
- 'killall' - remove all enemies currently on the map
- 'clear' - instantly win the level
- 'lose' - instanly trigger game over screen

## Original Low-Bars & Completion Status
- Grid Movement + No U-turn restriction - Complete
- Tethered Cheese with tile offset - Complete
- Cheese HP and Collision detection - Complete
- Yarn Ball Projectile Attack - Complete
- Catnip Powerup - Complete
- Chaser Mice - Complete
- Dijkstra pathfinding + Heat map around player - Complete
- Sentry/Patrolling Mice - Incomplete
- Interactives (switches, doors, pressure plate) - Partially Complete
- At least 2 well through out levels - Complete
- HUD - Complete
- Console cheats- Complete

## High-Bars & Completion Status
- Ambusher Enemy - Incomplete
- Projectile attack upgrades - Incomplete
- Additional Puzzle Elements - Partially Complete
- Conveyor Belt Tiles - Incomplete
- Crumbling tiles - Incomplete
- Final Boss Puzzle - Incomplete

## License

MIT License

Copyright (c) 2025 David Hoang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

  
