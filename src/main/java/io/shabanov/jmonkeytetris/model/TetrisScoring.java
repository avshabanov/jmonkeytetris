package io.shabanov.jmonkeytetris.model;

/**
 * Compute scoring as per original Nintendo scoring.
 * See also <a href="https://tetris.wiki/Scoring">Tetris Scoring</a>.
 */
public final class TetrisScoring {
    private static final int[] POINTS_PER_LINES = {40, 100, 300, 1200};

    private int currentScore;
    private int difficultyLevel;

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public void clearLines(int numberOfLines) {
        int pos = numberOfLines - 1;
        if (pos < 0) {
            return;
        }
        if (pos >= POINTS_PER_LINES.length) {
            pos = POINTS_PER_LINES.length - 1;
        }
        currentScore += POINTS_PER_LINES[pos] * (difficultyLevel + 1);
    }
}
