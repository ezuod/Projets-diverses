package fr.ubx.poo.ugarden.game;


import fr.ubx.poo.ugarden.go.decor.Decor;
import fr.ubx.poo.ugarden.go.decor.ObjectsDecor;
import fr.ubx.poo.ugarden.go.personage.Bee;

import java.util.*;

public interface Map {
    int width();
    int height();

    Decor get(Position position);
    void remove(Position position);
    Collection<Decor> values();
    ArrayList<Bee> getBees();

    ArrayList<ObjectsDecor.Door> getDoors();

    boolean inside(Position nextPos);

    void set(Position position, Decor decor);
}
