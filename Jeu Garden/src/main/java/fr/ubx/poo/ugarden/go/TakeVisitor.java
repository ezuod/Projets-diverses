package fr.ubx.poo.ugarden.go;

import fr.ubx.poo.ugarden.go.personage.Bee;

public interface TakeVisitor {

    default void take(Bee bonus) {}

}
