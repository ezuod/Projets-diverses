package fr.ubx.poo.ugarden.go.decor;
import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.go.GameObject;
import fr.ubx.poo.ugarden.go.Takeable;
import fr.ubx.poo.ugarden.go.Walkable;
import fr.ubx.poo.ugarden.go.bonus.Bonus;


public abstract class Decor extends GameObject implements Walkable, Takeable  {

    private Bonus bonus;

    public Decor(Position position) {
        super(position);
    }

    public Bonus getBonus() {
        return bonus;
    }

    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
    }
}