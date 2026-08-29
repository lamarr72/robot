package gui;

/**
* Перечисление уровней сложности.
* Инкапсулирует множетели для скорости, спавна препятствий и начисления очков.
*/
public enum DifficultyLevel {
    EASY(0.8, 1.5, 0.5, "Легко"),
    NORMAL(1.0, 1.0, 1.0, "Нормально"),
    HARD(1.5, 0.8, 2.0, "Сложно");

    private final double speedMultiplier;
    private final double spawnRateMultiplier;
    private final double scoreMultiplier;
    private final String displayName;

    DifficultyLevel(double speedMultiplier, double spawnRateMultiplier, double scoreMultiplier, String displayName) {
        this.speedMultiplier = speedMultiplier;
        this.spawnRateMultiplier = spawnRateMultiplier;
        this.scoreMultiplier = scoreMultiplier;
        this.displayName = displayName;
    }

    public double getSpeedMultiplier() { return speedMultiplier; }
    public double getSpawnRateMultiplier() { return spawnRateMultiplier; }
    public double getScoreMultiplier() { return scoreMultiplier; }
    public String getDisplayName() { return displayName; }
}