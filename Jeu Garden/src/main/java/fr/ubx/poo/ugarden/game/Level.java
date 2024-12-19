package fr.ubx.poo.ugarden.game;

import fr.ubx.poo.ugarden.go.bonus.ObjectsBonus;
import fr.ubx.poo.ugarden.go.decor.*;
import fr.ubx.poo.ugarden.go.decor.ObjectsDecor;
import fr.ubx.poo.ugarden.go.personage.Bee;
import fr.ubx.poo.ugarden.launcher.MapEntity;
import fr.ubx.poo.ugarden.launcher.MapLevel;

import java.util.*;

public class Level implements Map {

    private final int level;
    private final int width;

    private final int height;

    private final java.util.Map<Position, Decor> decors = new HashMap<>();

    private final ArrayList<Bee> beeList = new ArrayList<>();

    private final ArrayList<ObjectsDecor.Door> doorList = new ArrayList<>(); // Ajout de la déclaration de la liste de portes



    public Level(Game game, int level, MapLevel entities) {
        this.level = level;
        this.width = entities.width();
        this.height = entities.height();

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                Position position = new Position(level, i, j);
                MapEntity mapEntity = entities.get(i, j);
                switch (mapEntity) {
                    case Grass -> decors.put(position, new ObjectsDecor.Grass(position));
                    case Tree -> decors.put(position, new ObjectsDecor.Tree(position));
                    case Carrots -> decors.put(position, new ObjectsDecor.Carrots(position));
                    case Flowers -> decors.put(position, new ObjectsDecor.Flowers(position));
                    case Land -> decors.put(position, new ObjectsDecor.Land(position));
                    case Apple -> {
                        Decor grass = new ObjectsDecor.Grass(position);
                        grass.setBonus(new ObjectsBonus.Apple(position, grass));
                        decors.put(position, grass);
                    }
                    case PoisonedApple -> {
                        Decor grass = new ObjectsDecor.Grass(position);
                        grass.setBonus(new ObjectsBonus.PoisonedApple(position, grass));
                        decors.put(position, grass);
                    }
                    case Heart -> {
                        Decor grass = new ObjectsDecor.Grass(position);
                        grass.setBonus(new ObjectsBonus.Heart(position, grass));
                        decors.put(position, grass);
                    }
                    case Key -> {
                        Decor grass = new ObjectsDecor.Grass(position);
                        grass.setBonus(new ObjectsBonus.Key(position, grass));
                        decors.put(position, grass);
                    }
                    case Princess -> {
                        Decor grass = new ObjectsDecor.Grass(position);
                        grass.setBonus(new ObjectsBonus.Princess(position, grass));
                        decors.put(position, grass);
                    }
                    case Bee -> {
                        Decor grass = new ObjectsDecor.Grass(position);
                        beeList.add(beeList.size(), new Bee(game, new Position(this.level, i, j)));
                        decors.put(position, grass);
                    }
                    case DoorNextClosed -> {
                        ObjectsDecor.Door door = new ObjectsDecor.Door(position);
                        decors.put(position, door);
                        door.setDirectionDoor(1);
                        doorList.add(doorList.size(), door);
                    }
                    case DoorNextOpened -> {
                        ObjectsDecor.Door door = new ObjectsDecor.Door(position);
                        decors.put(position, door);
                        door.setOpen();
                        door.setDirectionDoor(1);
                        doorList.add(doorList.size(), door);
                    }
                    case DoorPrevOpened -> {
                        ObjectsDecor.Door door = new ObjectsDecor.Door(position);
                        decors.put(position, door);
                        door.setOpen();
                        door.setDirectionDoor(-1);
                        doorList.add(doorList.size(), door);
                    }
                    default -> throw new RuntimeException("EntityCode " + mapEntity.name() + " not processed");
                }
            }
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    public Decor get(Position position) {
        return decors.get(position);
    }

    @Override
    public void remove(Position position) {
        decors.remove(position);
    }

    public Collection<Decor> values() {
        return decors.values();
    }

    @Override
    public ArrayList<Bee> getBees() {
        return beeList;
    }
    public ArrayList<ObjectsDecor.Door> getDoors() {
        return doorList;
    }
    @Override
    public boolean inside(Position position) {
        return position.level() != level || position.x() < 0 || position.x() >= width || position.y() < 0 || position.y() >= height;
    }

    @Override
    public void set(Position position, Decor decor) {
        if (inside(position))
            throw new IllegalArgumentException("Illegal Position");
        if (decor != null)
            decors.put(position, decor);
    }


}
