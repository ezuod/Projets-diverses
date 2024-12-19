package fr.ubx.poo.ugarden.view;

import fr.ubx.poo.ugarden.game.Direction;
import javafx.scene.image.Image;

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

public class ImageResourceFactory {

    private final Map<ImageResource, Image> cache;

    private ImageResourceFactory() {
        cache = new Hashtable<>(ImageResource.size);
    }


    private static class LoadSingleton {
        static final ImageResourceFactory INSTANCE = new ImageResourceFactory();
    }

    public static ImageResourceFactory getInstance() {
        return ImageResourceFactory.LoadSingleton.INSTANCE;
    }


    private Image loadImage(ImageResource imageResource) {
        String fileName = imageResource.getFileName();
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/" + fileName)));
            if (image.getWidth() != ImageResource.size && image.getHeight() != ImageResource.size) {
                String msg = "File " + fileName + " does not have the correct size " + image.getWidth() + " x " + image.getHeight();
                throw new RuntimeException(msg);
            }
            return image;
        } catch (NullPointerException e) {
            System.err.println("Resource not found : " + fileName);
            throw e;
        }
    }

    public Image get(ImageResource imageResource) {
        Image image = cache.get(imageResource);
        if (image == null) {
            image = loadImage(imageResource);
            cache.put(imageResource, image);
        }
        return image;
    }

    public Image getDigit(int i) {
        if (i < 0 || i > 9) throw new IllegalArgumentException("Digit must be in [0-9]");
        return get(ImageResource.valueOf("DIGIT_" + i));
    }

    public Image getPlayer(Direction direction) {
        return get(ImageResource.valueOf("PLAYER_" + direction));
    }

    public Image getBee(Direction direction, Direction previousDirection) {
        ImageResource resource;
        if (direction == Direction.NONE && previousDirection != null) {
            direction = previousDirection;
        }
        resource = switch (direction) {
            case UP -> ImageResource.BEE_UP;
            case RIGHT -> ImageResource.BEE_RIGHT;
            case DOWN -> ImageResource.BEE_DOWN;
            case LEFT -> ImageResource.BEE_LEFT;
            case NONE -> ImageResource.GRASS; // Utilisez une image par défaut pour la direction NONE
        };
        return get(resource);
    }






}
