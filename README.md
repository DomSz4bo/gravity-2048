# Gravity 2048

Semester project for the _Programming (4) - Java_ course


## About the Game

This application is a modified version of the puzzle game 2048. The objective is to merge blocks with identical values to achieve the highest possible score. As in the classic version, the block values are powers of 2.

The key difference lies in the movement, instead of blocks sliding within a grid, they fall freely within the container. New blocks are generated at a constant height at the top of the screen. The player must horizontally position the new block, and upon release, it falls towards the bottom of the container under the influence of "gravity".

The game ends if any existing block exceeds the marked line at the top of the container when a new block is being released.


## Controls 🎮

The player can control the placement of the block using either a mouse or a keyboard.

### Mouse:
- **Move block**: Click and drag the block with the left mouse button.
- **Release block**: Release the left mouse button to let the block fall.

### Keyboard:
- **Move left**: Left arrow key.
- **Move right**: Right arrow key.
- **Release block**: Down arrow key or Spacebar.


## Spúšťanie
In IntelliJ, run the project through the `GameApp` class.

---

### Credits
Illustrations used in endgame dialog windows:
[Pódium](https://www.freepik.com/icon/podium_7921939)
and
[Game Over](https://www.flaticon.com/free-icon/game-over_6851342?term=game+over&page=1&position=3&origin=tag&related_id=6851342).
