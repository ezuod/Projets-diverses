package fr.ubx.poo.ugarden.go;

import fr.ubx.poo.ugarden.go.personage.Bee;
import fr.ubx.poo.ugarden.go.personage.Player;

public interface Walkable {
    default boolean walkableBy(Player player) { return false; }

    default boolean walkableBy(Bee bee) { return true; }

    default int energyConsumptionWalk() { return 0; }
}
