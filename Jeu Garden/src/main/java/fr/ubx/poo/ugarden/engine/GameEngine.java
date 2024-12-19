/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ugarden.engine;

import fr.ubx.poo.ugarden.game.Direction;
import fr.ubx.poo.ugarden.game.Game;
import fr.ubx.poo.ugarden.go.decor.ObjectsDecor;
import fr.ubx.poo.ugarden.go.personage.Bee;
import fr.ubx.poo.ugarden.go.personage.Player;
import fr.ubx.poo.ugarden.view.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;


public final class GameEngine {

    private static AnimationTimer gameLoop;
    private final Game game;
    private final Player player;
    private final List<Sprite> sprites = new LinkedList<>();
    private final Set<Sprite> cleanUpSprites = new HashSet<>();
    private final Stage stage;
    private List<Bee> bees;
    private ArrayList<ObjectsDecor.Door> doors;
    private StatusBar statusBar;
    private Input input;
    private Timer timerIsDying;

    public GameEngine(Game game, final Stage stage) {
        this.stage = stage;
        this.game = game;
        this.player = game.getPlayer();
        this.bees = game.world().getGrid(game.world().currentLevel()).getBees();
        this.doors = game.world().getGrid().getDoors();
        initialize();
        buildAndSetGameLoop();
    }

    private void initialize() {
        Group root = new Group();
        Pane layer = new Pane();

        int height = game.world().getGrid().height();
        int width = game.world().getGrid().width();
        int sceneWidth = width * ImageResource.size;
        int sceneHeight = height * ImageResource.size;
        Scene scene = new Scene(root, sceneWidth, sceneHeight + StatusBar.height);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm());

        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.hide();
        stage.show();

        input = new Input(scene);
        root.getChildren().add(layer);
        statusBar = new StatusBar(root, sceneWidth, sceneHeight);

        // Create sprites
        int currentLevel = game.world().currentLevel();

        for (var decor : game.world().getGrid().values()) {
            sprites.add(SpriteFactory.create(layer, decor));
            decor.setModified(true);
            var bonus = decor.getBonus();
            if (bonus != null) {
                sprites.add(SpriteFactory.create(layer, bonus));
                bonus.setModified(true);
            }
        }

        sprites.add(new SpritePlayer(layer, player));

        for(Bee bee : bees)
            sprites.add(new SpriteBee(layer, bee));

    }

    void buildAndSetGameLoop() {
        gameLoop = new AnimationTimer() {
            public void handle(long now) {
                checkLevel();

                // Check keyboard actions
                processInput();


                // Do actions
                update(now);
                checkCollision(now);


                // Graphic update
                cleanupSprites();
                render();
                statusBar.update(game);
            }
        };
    }


    private void checkLevel() {

        if (game.isSwitchLevelRequested()) {
            // Récupérer le niveau de départ et le niveau de destination
            int oldLevel = game.world().currentLevel();
            int level = game.getSwitchLevel();

            // Vider la liste des sprites et nettoyer les sprites existants
            sprites.clear();
            cleanupSprites();

            // Changer le niveau courant dans le monde
            game.world().setCurrentLevel(level);

            // Effacer la demande de changement de niveau
            game.clearSwitchLevel();

            // Mettre à jour les listes de Bee et Door
            this.bees = game.world().getGrid().getBees();
            this.doors = game.world().getGrid().getDoors();
            for(ObjectsDecor.Door door : doors)// Find the position of the door to reach
                if(door.isOpen())
                    if (oldLevel > game.world().currentLevel() && door.getDirectionDoor() == 1)
                        player.setPosition(door.getPosition());// Set the position of the player
                    else if (oldLevel < game.world().currentLevel() && door.getDirectionDoor() == -1)
                        player.setPosition(door.getPosition());// Set the position of the player
            // Fermer la fenêtre actuelle et initialiser la nouvelle fenêtre
            stage.close();
            initialize();
        }

    }

    private void checkCollision(long now) {
        // Vérifier si le timer de l'état mourant du joueur n'est pas initialisé ou s'il n'est pas en cours d'exécution
        if (timerIsDying == null || !timerIsDying.isRunning()) {
            // Réinitialiser l'état mourant et la modification du joueur
            player.setDying(false);
            player.setModified(true);

            // Parcourir la liste des abeilles
            for (int i = 0; i < bees.size(); i++) {
                // Vérifier si la position de l'abeille correspond à la position du joueur
                if (bees.get(i).getPosition().equals(player.getPosition())) {
                    // Faire en sorte que le joueur prenne l'abeille (par exemple, la tuer)
                    player.take(bees.get(i));
                    // Supprimer l'abeille de la liste des abeilles
                    bees.remove(bees.get(i));

                    // Initialiser et démarrer le timer pour la durée d'invincibilité du joueur
                    timerIsDying = new Timer(game.configuration().playerInvincibilityDuration());
                    timerIsDying.start();
                    // Définir l'état mourant du joueur sur true (le joueur est invincible pendant un certain temps)
                    player.setDying(true);
                }
            }
        } else {
            // Mettre à jour le timer de l'état mourant du joueur
            timerIsDying.update(now);
        }
    }


    private void processInput() {
        // Vérifie si l'utilisateur a demandé à quitter le jeu
        if (input.isExit()) {
            // Arrête la boucle de jeu
            gameLoop.stop();
            // Ferme l'application JavaFX
            Platform.exit();
            // Force la fermeture de l'application Java
            System.exit(0);
        } else {
            // Vérifie si l'utilisateur a demandé un mouvement
            Direction requestedDirection = null;
            if (input.isMoveDown()) {
                requestedDirection = Direction.DOWN;
            } else if (input.isMoveLeft()) {
                requestedDirection = Direction.LEFT;
            } else if (input.isMoveRight()) {
                requestedDirection = Direction.RIGHT;
            } else if (input.isMoveUp()) {
                requestedDirection = Direction.UP;
            }
            if (requestedDirection != null) {
                // Demande au personnage de se déplacer dans la direction demandée
                player.requestMove(requestedDirection);
            }
            // Efface toutes les entrées clavier traitées
            input.clear();
        }
    }



    private void showMessage(String msg, Color color) {
        Text waitingForKey = new Text(msg);
        waitingForKey.setTextAlignment(TextAlignment.CENTER);
        waitingForKey.setFont(new Font(60));
        waitingForKey.setFill(color);
        StackPane root = new StackPane();
        root.getChildren().add(waitingForKey);
        Scene scene = new Scene(root, 400, 200, Color.WHITE);
        stage.setScene(scene);
        input = new Input(scene);
        stage.show();
        new AnimationTimer() {
            public void handle(long now) {
                processInput();
            }
        }.start();
    }


    private void update(long now) {
        player.update(now);
        for(Bee bee : bees)
            bee.update(now);
        if (player.getLives() == 0) {
            gameLoop.stop();
            showMessage("Perdu !", Color.RED);
        } else if(player.getEnergy() <= 0){
            gameLoop.stop();
            showMessage("Perdu !", Color.RED);
        } else if(player.getIsWin()){
            gameLoop.stop();
            showMessage("Gagné !", Color.GREEN);
        }
    }

    public void cleanupSprites() {
        sprites.forEach(sprite -> {
            if (sprite.getGameObject().isDeleted()) {
                cleanUpSprites.add(sprite);
            }
        });
        cleanUpSprites.forEach(Sprite::remove);
        sprites.removeAll(cleanUpSprites);
        cleanUpSprites.clear();
    }

    private void render() {
        sprites.forEach(Sprite::render);
    }

    public void start() {
        gameLoop.start();
    }
}