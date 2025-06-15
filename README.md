# ChessGame

## Overview
A lightweight Java-based chess application featuring:
- **Player vs. Player** and **Player vs. AI** modes  
- A simple **1‑ply heuristic AI** (material, piece‑square tables, pawn structure, king safety, mobility, threats)  
- **ToolShed mechanic**: captured pieces can be “transplanted” back onto the board  
- A Swing‑based GUI with drag‑and‑drop and move highlighting  

## Requirements
- Java 8 or higher  
- No external dependencies  

## Compile & Run
```bash
# From project root, compile all sources into “out”:
javac -d out src/com/chessgame/**/*.java

# Launch the application by running the main class:
java -cp out main
