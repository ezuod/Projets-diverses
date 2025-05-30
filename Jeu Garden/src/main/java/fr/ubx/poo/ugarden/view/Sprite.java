/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ugarden.view;

import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.go.GameObject;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Sprite {

    public static final int size = 40;
    private final Pane layer;
    private final GameObject gameObject;
    private ImageView imageView;
    private Image image;
    private Effect effect;

    public Sprite(Pane layer, Image image, GameObject gameObject) {
        this.layer = layer;
        this.image = image;
        this.gameObject = gameObject;
    }

    public GameObject getGameObject() {
        return gameObject;
    }

    public final void setImage(Image image, Effect effect) {
        if (this.image == null || this.image != image || this.effect != effect) {
            this.image = image;
            this.effect = effect;
        }
    }

    public final void setImage(Image image) {
        setImage(image, null);
    }

    public void updateImage() {

    }


    public Position getPosition() {
        return getGameObject().getPosition();
    }

    public final void render() {
        if (gameObject.isModified()) {
            if (imageView != null) {
                remove();
            }
            updateImage();
            imageView = new ImageView(this.image);
            imageView.setEffect(effect);
            imageView.setX(getPosition().x() * size);
            imageView.setY(getPosition().y() * size);
            layer.getChildren().add(imageView);
            gameObject.setModified(false);
        }

    }
    public void setOpacity(double opacity) {
        if (imageView != null) {
            imageView.setOpacity(opacity);
        }
    }
    public final void remove() {
        layer.getChildren().remove(imageView);
        imageView = null;
    }
}
