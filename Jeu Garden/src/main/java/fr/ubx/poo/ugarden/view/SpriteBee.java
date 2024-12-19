package fr.ubx.poo.ugarden.view;

import fr.ubx.poo.ugarden.game.Direction;
import fr.ubx.poo.ugarden.go.personage.Bee;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;


public class SpriteBee extends Sprite {

    private Direction previousDirection;
    private final ImageResourceFactory imageResourceFactory;

    public SpriteBee(Pane layer, Bee bee) {
        super(layer, null, bee);
        imageResourceFactory = ImageResourceFactory.getInstance();
        updateImage();
    }

    public Image getImage(Direction direction) {
        if (direction != Direction.NONE) {
            previousDirection = direction;
        }
        return imageResourceFactory.getBee(direction, previousDirection);
    }

    @Override
    public void updateImage() {
        Bee bee = (Bee) getGameObject();
        Image image = getImage(bee.getDirection());
        setImage(image);
    }


}

