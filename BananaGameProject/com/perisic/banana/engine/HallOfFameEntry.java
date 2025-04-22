package com.perisic.banana.engine;

public class HallOfFameEntry {
    private final String player;
    private final int highestScore;
    private final int winStreak;

    public HallOfFameEntry(String player, int highestScore, int winStreak) {
        this.player = player;
        this.highestScore = highestScore;
        this.winStreak = winStreak;
    }

    public String getPlayer() {
        return player;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public int getWinStreak() {
        return winStreak;
    }
}
