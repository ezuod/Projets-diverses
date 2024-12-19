package fr.ubx.poo.ugarden.game;

public interface World {
    int levels();
    int currentLevel();
    void setCurrentLevel(int level);

    Map getGrid(int level);

    Map getGrid();




}
