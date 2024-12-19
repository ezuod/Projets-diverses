/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ugarden.view;

import fr.ubx.poo.ugarden.go.GameObject;
import fr.ubx.poo.ugarden.go.bonus.*;
import fr.ubx.poo.ugarden.go.decor.ObjectsDecor;
import javafx.scene.layout.Pane;

import static fr.ubx.poo.ugarden.view.ImageResource.*;


public final class SpriteFactory {

    public static Sprite create(Pane layer, GameObject gameObject) {
        ImageResourceFactory factory = ImageResourceFactory.getInstance();
        if (gameObject instanceof ObjectsDecor.Grass)           // Grass
            return new Sprite(layer, factory.get(GRASS), gameObject);
        if (gameObject instanceof ObjectsDecor.Land)            // Land
            return new Sprite(layer, factory.get(LAND),gameObject);
        if (gameObject instanceof ObjectsDecor.Tree)            // Tree
            return new Sprite(layer, factory.get(TREE), gameObject);
        if (gameObject instanceof ObjectsDecor.Carrots)         // Carrots
            return new Sprite(layer, factory.get(CARROTS),gameObject);
        if (gameObject instanceof ObjectsDecor.Flowers)         // Flowers
            return new Sprite(layer, factory.get(FLOWERS),gameObject);
        if (gameObject instanceof ObjectsBonus.PoisonedApple)   // PoisonedApple
            return new Sprite(layer, factory.get(POISONED_APPLE), gameObject);
        if (gameObject instanceof ObjectsBonus.Apple)           // Apple
            return new Sprite(layer, factory.get(APPLE), gameObject);
        if (gameObject instanceof ObjectsBonus.Heart)           // Heart
            return new Sprite(layer, factory.get(HEART), gameObject);
        if (gameObject instanceof ObjectsBonus.Key)             // Key
            return new Sprite(layer, factory.get(KEY), gameObject);
        if (gameObject instanceof ObjectsBonus.Princess)        // Princess
            return new Sprite(layer, factory.get(PRINCESS),gameObject);
        if (gameObject instanceof ObjectsDecor.Door){           // Door
            if(!((ObjectsDecor.Door) gameObject).isOpen())
                return new Sprite(layer, factory.get(DOOR_CLOSED), gameObject);
            else
                return new Sprite(layer, factory.get(DOOR_OPENED), gameObject);
        }
        throw new RuntimeException("Unsupported sprite for decor " + gameObject);
    }
}
