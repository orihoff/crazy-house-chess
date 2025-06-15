package com.chessgame.Ai;

import com.chessgame.Board.Board;

/**
 * An interface defining a candidate move for the AI.
 * This candidate knows how to apply itself to a cloned board (for evaluation purposes).
 */
public interface MoveCandidate {
    /**
     * Applies this candidate move to the given board.
     * The board should be cloned before applying the move so that the original board remains unaffected.
     *
     * @param board the board on which to apply the move.
     */
    void apply(Board board);

    /**
     * Returns a short description of the move for debugging purposes.
     *
     * @return a brief description of the move.
     */
    String getDescription();
}
