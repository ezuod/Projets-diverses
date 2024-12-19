/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ugarden.go.personage;

import fr.ubx.poo.ugarden.engine.Timer;
import fr.ubx.poo.ugarden.game.Direction;
import fr.ubx.poo.ugarden.game.Game;
import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.go.GameObject;
import fr.ubx.poo.ugarden.go.Movable;
import fr.ubx.poo.ugarden.go.TakeVisitor;
import fr.ubx.poo.ugarden.go.WalkVisitor;
import fr.ubx.poo.ugarden.go.decor.Decor;
import fr.ubx.poo.ugarden.go.bonus.*;
import fr.ubx.poo.ugarden.go.decor.ObjectsDecor;


public class Player extends GameObject implements Movable, TakeVisitor, WalkVisitor {

    private Direction direction;
    private final Timer diseaseTimer;

    private final Timer energyRecoverTimer;
    private boolean moveRequested = false;

    private boolean isWin;

    private boolean isDying;
    private int lives;

    private int keys;
    private int diseaseLevel;

    private long lastDiseaseDecreaseTime = 0;
    private int energy;

    public Timer poisonedAppleEffectTimer = null;


    public int getLives() {
        return lives;
    }

    public int getEnergy() {
        return energy;
    }

    public int getKeys() {
        return keys;
    }

    public boolean getIsDying(){return isDying;}

    public int getDiseaseLevel() {
        return diseaseLevel;
    }

    public boolean getIsWin(){return isWin;}


    public Direction getDirection() {
        return direction;
    }

    public void requestMove(Direction direction) {
        if (direction != this.direction) {
            this.direction = direction;
            setModified(true);
        }
        moveRequested = true;
    }

    @Override
    public final boolean canMove(Direction direction) {
        // Calculer la position suivante du joueur en utilisant la direction donnée
        Position nextPos = direction.nextPosition(getPosition());
        // Récupérer l'objet de décor à la position suivante
        Decor next = game.world().getGrid().get(nextPos);

        // Si l'objet de décor à la position suivante est une porte et que le joueur possède une clé
        if (next instanceof ObjectsDecor.Door && hasItem("key")) {
            // Ouvrir la porte
            ((ObjectsDecor.Door) next).setOpen();
            // Décrémenter le nombre de clés
            keys--;
            // Renvoyer true pour indiquer que le joueur peut se déplacer
            return true;
        }

        // Vérifier si la position suivante est à l'intérieur des limites de la carte,
        // si le niveau d'énergie du joueur est suffisant pour se déplacer et
        // si l'objet de décor à la position suivante est traversable par le joueur
        return next != null && energy >= next.energyConsumptionWalk() * diseaseLevel && next.walkableBy(game.getPlayer());
    }




    private long lastMoveTime = 0;

    public void update(long now) {
        // Vérifie si un mouvement a été demandé
        if (moveRequested) {
            // Vérifie si l'objet peut effectuer le mouvement dans la direction donnée
            if (canMove(direction)) {
                // Effectue le mouvement
                doMove(direction);
                // Empêche la récupération d'énergie pendant un certain temps après un mouvement
                energyRecoverTimer.start();
                // Enregistre le temps du dernier mouvement
                lastMoveTime = now;
            }
        } else {
            // Vérifie si le personnage est immobile depuis un certain temps et si le timer de récupération d'énergie n'est pas en cours d'exécution
            if (!energyRecoverTimer.isRunning() && now - lastMoveTime >= 2_000_000_000L) {
                // Augmente l'énergie du joueur et démarre le timer de récupération d'énergie
                energyRecover();
                energyRecoverTimer.start();
            } else {
                // Met à jour le timer de récupération d'énergie
                energyRecoverTimer.update(now);
            }
        }
        // Réinitialise moveRequested à false, car l'objet a déjà effectué le mouvement demandé s'il y en avait un
        moveRequested = false;

        // Si le timer de maladie n'est pas en cours d'exécution et que le niveau de maladie est supérieur à 0, diminue le niveau de maladie toutes les 5 secondes
        if (!diseaseTimer.isRunning() && diseaseLevel > 0 && now - lastDiseaseDecreaseTime >= 5_000_000_000L) {
            decreaseDiseaseLevel();
            lastDiseaseDecreaseTime = now;
        }
        // Met à jour le timer de maladie
        diseaseTimer.update(now);
    }



    public Player(Game game, Position position) {
        super(game, position);
        this.direction = Direction.DOWN;
        this.lives = game.configuration().playerLives();
        this.energy = game.configuration().playerEnergy();
        this.diseaseTimer = new Timer(5);
        this.energyRecoverTimer = new Timer(game.configuration().energyRecoverDuration());
        this.diseaseLevel = 1;
        this.keys = 0;
    }

    public void take(GameObject obj) {
        if (obj instanceof Bonus) {
            // Convertir l'objet en type Bonus et appliquer l'effet sur le joueur en cours
            ((Bonus) obj).applyEffect(this);
            // Supprimer l'objet de la carte
            obj.remove();
        } else if (obj instanceof Bee) {
            // Décrémenter le nombre de vies du joueur en cours
            lives--;
            // Supprimer l'abeille de la carte
            obj.remove();
        } else if (obj instanceof ObjectsDecor.Door) {
            // Récupérer la direction de la porte et passer au niveau suivant
            game.requestSwitchLevel(game.world().currentLevel() + ((ObjectsDecor.Door) obj).getDirectionDoor());
            // Mettre à jour le nombre de clés du joueur en cours
            this.keys = Math.min(this.keys, 0);
            // Supprimer la porte de la carte
            obj.remove();
        }
    }




    @Override
    public void doMove(Direction direction) {
        // Cette méthode est appelée uniquement si le déplacement est possible, ne vérifiez pas à nouveau
        Position nextPos = direction.nextPosition(getPosition());
        Decor next = game.world().getGrid().get(nextPos);

        // Met à jour la position de l'objet à la nouvelle position
        setPosition(nextPos);

        // Vérifie si un objet Decor est présent à la nouvelle position
        if (next != null) {
            // Appelle la méthode takenBy sur l'objet Decor pour signaler qu'il a été pris ou traversé
            next.takenBy(this);

            // Vérifie si l'objet Decor est une instance de Grass, Land ou Carrots
            // et réduit l'énergie du personnage en fonction de la consommation d'énergie définie pour marcher sur cet objet
            if (next instanceof ObjectsDecor.Grass) {
                decreaseEnergy(next.energyConsumptionWalk());
            } else if (next instanceof ObjectsDecor.Land) {
                decreaseEnergy(next.energyConsumptionWalk());
            } else if (next instanceof ObjectsDecor.Carrots) {
                decreaseEnergy(next.energyConsumptionWalk());
            }
        }

        // Vérifie si l'énergie du personnage est inférieure ou égale à zéro
        if (getEnergy() <= 0) {
            // Si l'énergie est épuisée, déclenche la fin de la partie (perte)
            lose();
        }
    }


    /**
     * Méthode de gestion de la vie
     */
    public void addLife() {
        lives++;
    }

    public void setDying(boolean dying) {
        isDying = dying;
    }

    /**
     * Méthode des clés
     */

    public void addKey() {
        keys++;
    }

    public boolean hasItem(String itemName) {
        return itemName.equals("key") && keys > 0;
    }


    /**
     * Méthodes de gestion de l'énergie
     */
    public void energyRecover() {
        energy += game.configuration().energyRecoverDuration();
        if (energy > 100) {
            energy = 100;
        }
    }

    public void applyEnergyBoost() {
        energy = Math.min(energy + game.configuration().energyBoost(), 100);
    }

    public void decreaseEnergy(int amount) {
        energy -= amount;
    }

    /**
     * Méthodes de gestion du niveau de maladie
     */

    public void increaseDiseaseLevel() {
        diseaseLevel++;
        diseaseTimer.start();
    }

    public void decreaseDiseaseLevel() {
        if (diseaseLevel > 0) {
            diseaseLevel--;
        }
    }

    /**
     * Méthodes de fin de partie
     */

    public void lose() {
        System.out.println("Dommage! Vous avez épuisé toute votre énergie et perdu la partie.");
        // Ajoutez ici le code pour terminer la partie et afficher un écran de défaite
    }

    public void setIsWin(boolean isWin) {
        this.isWin = isWin;
    }


}
