package fr.ubx.poo.ugarden.go.personage;

import fr.ubx.poo.ugarden.engine.Timer;
import fr.ubx.poo.ugarden.game.Direction;
import fr.ubx.poo.ugarden.game.Game;
import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.go.GameObject;
import fr.ubx.poo.ugarden.go.Movable;
import fr.ubx.poo.ugarden.go.Takeable;
import fr.ubx.poo.ugarden.go.decor.Decor;

public class Bee extends GameObject implements Movable, Takeable {
    private Direction direction;
    private Timer timerBeeNextMove;


    public Direction getDirection() {
        return direction;
    }

    public Bee(Game game, Position position) {
        super(game,position);
        this.direction = Direction.DOWN;
    }

    public void update(long now) {
        // Vérifie si le timer pour le prochain mouvement de l'abeille n'est pas initialisé
        if (timerBeeNextMove == null) {
            // Initialise le timer avec la fréquence de déplacement des abeilles définie dans la configuration
            timerBeeNextMove = new Timer(game.configuration().beeMoveFrequency());
            // Démarre le timer
            timerBeeNextMove.start();
        }

        // Vérifie si le timer est en cours d'exécution
        if (timerBeeNextMove.isRunning()) {
            // Met à jour le timer avec le temps actuel
            timerBeeNextMove.update(now);
        } else {
            // Choisit une direction aléatoire pour l'abeille
            direction = Direction.random();

            // Vérifie si l'abeille peut se déplacer dans la direction choisie
            if (canMove(direction)) {
                // Fait bouger l'abeille dans la direction choisie
                doMove(direction);
                // Réinitialise le timer pour le prochain mouvement
                timerBeeNextMove = null;
            }
        }
    }


    @Override
    public boolean canMove(Direction direction) {
        // Récupérer la position suivante en fonction de la direction donnée
        Position nextPos = direction.nextPosition(getPosition());

        // Récupérer l'objet de décor à la position suivante
        Decor next = game.world().getGrid().get(nextPos);

        // Vérifier si la position suivante est dans les limites de la carte et si l'objet de décor permet le passage de l'abeille
        if (next != null && next.walkableBy(this)) {
            // Si l'objet de décor permet le passage de l'abeille, la méthode renvoie true
            return true;
        } else {
            // Sinon, la méthode renvoie false
            return false;
        }
    }



    @Override
    public void doMove(Direction direction) {
        Position nextPos = direction.nextPosition(getPosition());
        setPosition(nextPos);
    }



}
