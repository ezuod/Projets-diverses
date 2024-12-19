/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ugarden.view;

import fr.ubx.poo.ugarden.game.Direction;
import fr.ubx.poo.ugarden.go.personage.Player;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class SpritePlayer extends Sprite {
    private final ColorAdjust effect = new ColorAdjust();

    public SpritePlayer(Pane layer, Player player) {
        super(layer, null, player);
        effect.setBrightness(0.5); // Ajout de l'effet de transparence
        updateImage();
    }


    @Override
    public void updateImage() {
        Player player = (Player) getGameObject();
        if (player.getIsDying()) {
            effect.setBrightness(0.9);
            effect.setSaturation(0.5);
            super.setOpacity(0.3);
        } else {
            effect.setBrightness(0);
            effect.setSaturation(0);
            super.setOpacity(1.0);
        }
        Image image = getImage(player.getDirection());
        setImage(image, effect);
    }

    private Image getImage(Direction direction) {
        return ImageResourceFactory.getInstance().getPlayer(direction);
    }
}
