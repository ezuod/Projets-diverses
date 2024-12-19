/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ugarden.go.decor;

import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.go.bonus.Bonus;
import fr.ubx.poo.ugarden.go.personage.Bee;
import fr.ubx.poo.ugarden.go.personage.Player;

public abstract class ObjectsDecor extends Decor {

    public ObjectsDecor(Position position) {
        super(position);
    }

    @Override
    public boolean walkableBy(Player player) {
        return true;
    }

    @Override
    public void takenBy(Player player) {
        Bonus bonus = getBonus();
        if (bonus != null) {
            bonus.takenBy(player);
        }
    }

    /**
     * Object Tree
     */

    public static class Tree extends ObjectsDecor {
        public Tree(Position position) {
            super(position);
        }

        @Override
        public boolean walkableBy(Player player) {
            return false;
        }
    }

    /**
     * Object Flowers
     */

    public static class Flowers extends ObjectsDecor {
        public Flowers(Position position) {
            super(position);
        }

        @Override
        public boolean walkableBy(Player player) {
            return false;
        }
    }


    /**
     * Object Grass
     */
    public static class Grass extends ObjectsDecor {
        public Grass(Position position) {
            super(position);
        }

        @Override
        public int energyConsumptionWalk() {
            return 1;
        }
    }

    /**
     * Object Land
     */

    public static class Land extends ObjectsDecor {
        public Land(Position position) {
            super(position);
        }

        @Override
        public int energyConsumptionWalk() { return 2; }
    }

    /**
     * Object Carrots
     */

    public static class Carrots extends ObjectsDecor {
        public Carrots(Position position) {
            super(position);
        }

        @Override
        public int energyConsumptionWalk() { return 3; }
    }

    /**
     * Object Door
     */

    public static class Door extends ObjectsDecor {

        private boolean isOpen = false;
        private int directionDoor = 0;

        public boolean isOpen() {
            return isOpen;
        }

        public void setOpen() {
            isOpen = true;
        }

        public void setDirectionDoor(int directionDoor){
            this.directionDoor = directionDoor;
        }

        public int getDirectionDoor(){return this.directionDoor;}

        public Door(Position position) {
            super(position);
        }

        @Override
        public void takenBy(Player player) {
            player.take(this);
        }

        @Override
        public boolean walkableBy(Bee bee) {
            return false;
        }

    }

}

