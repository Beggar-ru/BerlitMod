package ru.beggar.berlit.spawner;

public class UpgradeStats {

    private final float speedBonus;
    private final int amountBonus;

    public UpgradeStats(float speedBonus, int amountBonus) {
        this.speedBonus = speedBonus;
        this.amountBonus = amountBonus;
    }

    public float getSpeedBonus() {
        return speedBonus;
    }

    public int getAmountBonus() {
        return amountBonus;
    }
}
