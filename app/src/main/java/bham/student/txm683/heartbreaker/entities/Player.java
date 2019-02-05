package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.entities.entityshapes.IsoscelesTrapezium;
import bham.student.txm683.heartbreaker.utils.Point;
import org.json.JSONException;

import java.text.ParseException;

public class Player extends MoveableEntity {

    private int maxCharge;
    private int currentMeleeCharge;
    private long lastChargeTime;
    private long currentChargeTime;

    private static final int TIME_BETWEEN_CHARGES = 25;

    private int attackCooldown;

    private int damageDealt;

    public Player(String name, Point spawnCoordinates, int size, float maxSpeed, int color){
        super(name, new IsoscelesTrapezium(spawnCoordinates, (size/2f), (size/2f), (size/2f), color), maxSpeed);

        damageDealt = 0;

        attackCooldown = 0;

        this.maxCharge = 25;

        //resetMeleeCharges();
    }

    public Player(String stateString) throws ParseException, JSONException {
        super(stateString);
    }

    /*private void resetMeleeCharges(){
        this.currentMeleeCharge = 0;

        this.lastChargeTime = 0;
        this.currentChargeTime = 0;
    }

    public void chargeMelee(){
        currentChargeTime = System.currentTimeMillis();

        if (currentMeleeCharge < maxCharge && attackCooldown == 0) {
            if (currentChargeTime - lastChargeTime > TIME_BETWEEN_CHARGES) {
                currentMeleeCharge += 1;
                lastChargeTime = currentChargeTime;
                Log.d(TAG, "adding charge, charge now at: " + currentMeleeCharge);

                if (currentMeleeCharge == 1 || currentMeleeCharge == maxCharge){
                    shape.contractWidth(1.1f);
                }
            } else {
                Log.d(TAG, "not enough time passed for charge (" + (currentChargeTime - lastChargeTime) + ")");
            }
        } else {
            Log.d(TAG, "already at max charge");
        }
    }

    *//**
     * Called when the melee button is released.
     *//*
    public void meleeAttack(){
        Log.d(TAG, "melee attack with charge of " + currentMeleeCharge);
        shape.returnToNormal();
        shape.contractWidth(0.8f);
        shape.contractHeight(1.4f);

        if (currentMeleeCharge > 0 && currentMeleeCharge <= maxCharge / 2){
            damageDealt = 1;
        } else if (currentMeleeCharge > 0){
            damageDealt = 3;
        }
        resetMeleeCharges();

        attackCooldown = 5;
        launchedAttack = true;
    }

    *//**
     * Called by collision manager at end of checks to mark collision as checked
     *//*
    public void resetAttack(){
        launchedAttack = false;
        damageDealt = 0;

    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public void tickCooldown(){
        attackCooldown -= 1;

        if (attackCooldown <= 0){
            attackCooldown = 0;
            shape.returnToNormal();
        }
    }

    public int getDamageFromMeleeAttack(){
        return damageDealt;
    }*/
}