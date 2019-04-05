package bham.student.txm683.framework.physics;

public interface Damageable {

    /**
     * @return The current health of the entity
     */
    int getHealth();

    /**
     * Sets the health of the entity to the given value.
     * Health is lower bounded by zero
     * @param health New health value for the entity
     */
    void setHealth(int health);

    /**
     * Inflicts the amount of damage given to the
     * @param damageToInflict Damage points to inflict
     * @return True if the entity's health has dropped to zero (or below)
     */
    boolean inflictDamage(int damageToInflict);

    /**
     * Will add the given value to the entity's current health value.
     * Health is lower bounded by zero.
     * @param healthToRestore Amount of health to restore
     */
    void restoreHealth(int healthToRestore);

    int getInitialHealth();
}
