package fr.ubx.poo.ugarden.go.bonus;

import fr.ubx.poo.ugarden.engine.Timer;
import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.go.decor.Decor;
import fr.ubx.poo.ugarden.go.personage.Player;

public class ObjectsBonus extends Bonus {
    public ObjectsBonus(Position position, Decor decor) {
        super(position, decor);
    }

    @Override
    public void applyEffect(Player player) {
        player.take(this);
    }

    /**
     * ObjectBonus APPLE
     */
    public static class Apple extends Bonus{
        public Apple(Position position, Decor decor) {
            super(position, decor);
        }

        public void applyEffect(Player player) {
            player.decreaseDiseaseLevel();
            player.energyRecover();
            player.applyEnergyBoost();
        }
    }

    /**
     * ObjectBonus HEART
     */

    public static class Heart extends Bonus {
        public Heart(Position position, Decor decor) {
            super(position, decor);
        }

        public void applyEffect(Player player) {
            player.addLife();
        }

    }

    /**
     * ObjectBonus POISONEDAPPLE
     */

    public static class PoisonedApple extends Bonus {
        public PoisonedApple(Position position, Decor decor) {
            super(position, decor);
        }

        public void applyEffect(Player player) {
            player.increaseDiseaseLevel();
            if (player.poisonedAppleEffectTimer == null) {
                player.poisonedAppleEffectTimer = new Timer(1000);
                player.poisonedAppleEffectTimer.start();
            } else {
                player.poisonedAppleEffectTimer.start();
            }
        }

    }

    /**
     * ObjectBonus KEY
     */
    public static class Key extends Bonus {
        public Key(Position position, Decor decor) {
            super(position, decor);
        }

        public void applyEffect(Player player) {
            player.addKey();
        }
    }

    /**
     * ObjectBonus PRINCESS
     */
    public static class Princess extends Bonus {
        public Princess(Position position, Decor decor) {
            super(position, decor);
        }

        public void applyEffect(Player player) {
            player.setIsWin(true);
        }
    }


}


