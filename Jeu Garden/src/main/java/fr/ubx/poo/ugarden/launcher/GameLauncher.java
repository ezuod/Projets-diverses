package fr.ubx.poo.ugarden.launcher;

import fr.ubx.poo.ugarden.game.*;
import java.util.ArrayList;
import java.util.Properties;

public class GameLauncher {

    private static class LoadSingleton {
        static final GameLauncher INSTANCE = new GameLauncher();
    }
    private GameLauncher() {}

    public static GameLauncher getInstance() {
        return LoadSingleton.INSTANCE;
    }

    private int integerProperty(Properties properties, String name, int defaultValue) {
        return Integer.parseInt(properties.getProperty(name, Integer.toString(defaultValue)));
    }

    private Configuration getConfiguration(Properties properties) {

        // Load parameters
        int playerLives = integerProperty(properties, "playerLives", 5);
        int playerInvincibilityDuration = integerProperty(properties, "playerInvincibilityDuration", 4);
        int beeMoveFrequency = integerProperty(properties, "beeMoveFrequency", 1);
        int playerEnergy = integerProperty(properties, "playerEnergy", 100);
        int energyBoost = integerProperty(properties, "energyBoost", 50);
        int energyRecoverDuration = integerProperty(properties, "energyRecoverDuration", 5);
        int diseaseDuration = integerProperty(properties, "diseaseDuration", 5);

        return new Configuration(playerLives, playerEnergy, energyBoost, playerInvincibilityDuration, beeMoveFrequency, energyRecoverDuration, diseaseDuration);
    }

    public Game load() {
        Properties emptyConfig = new Properties();
        MapLevel mapLevel = new MapLevelDefault();
        Position playerPosition = mapLevel.getPlayerPosition();
        if (playerPosition == null)
            throw new RuntimeException("Player not found");
        Configuration configuration = getConfiguration(emptyConfig);
        WorldLevels world = new WorldLevels(1);
        Game game = new Game(world, configuration, playerPosition);
        Map level = new Level(game, 1, mapLevel);
        world.put(1, level);
        return game;
    }

    /**
     * Charge un jeu à partir d'un objet Properties contenant les informations nécessaires.
     * @param properties Les propriétés contenant les informations nécessaires pour charger le jeu.
     * @return Un objet Game représentant le jeu chargé.
     */
    public Game load(Properties properties) {
        // On récupère les paramètres nécessaires
        boolean compression = Boolean.parseBoolean(properties.getProperty("compression"));
        int nbLevels = Integer.parseInt(properties.getProperty("levels"));

        // On charge chaque niveau dans une liste de MapLevel
        ArrayList<MapLevel> levels = new ArrayList<>(nbLevels);
        for (int i = 1; i <= nbLevels; i++) {
            String levelProperty = properties.getProperty("level" + i);
            MapLevel level = stringToMap(compression ? decompressString(levelProperty) : levelProperty);
            levels.add(level);
        }

        // On récupère la position du joueur dans le premier niveau
        Position playerPosition = levels.get(0).getPlayerPosition();
        if (playerPosition == null)
            throw new RuntimeException("Player not found");

        // On initialise le monde et le jeu
        Configuration configuration = getConfiguration(properties);
        WorldLevels world = new WorldLevels(1);
        Game game = new Game(world, configuration, playerPosition);

        // On ajoute chaque niveau au monde du jeu
        for (int index = 1; index <= levels.size(); index++) {
            Map map = new Level(game, index, levels.get(index - 1));
            world.put(index, map);
        }

        return game;
    }

    /**
     * Convertit une chaîne de caractères représentant une carte en un objet MapLevel.
     * @param string La chaîne de caractères représentant la carte.
     * @return Un objet MapLevel représentant la carte.
     */
    private MapLevel stringToMap(String string) {
        // On découpe la chaîne en lignes et en colonnes
        String[] tokens = string.split("x");
        int width = tokens[0].length();
        int height = tokens.length;
        MapLevel mapLevel = new MapLevel(width, height);

        // On parcourt chaque caractère de la chaîne pour initialiser la carte
        for (int i = 0; i < height; i++) {
            String line = tokens[i];
            for (int j = 0; j < width; j++) {
                char code = line.charAt(j);
                MapEntity entity = MapEntity.fromCode(code);
                mapLevel.set(j, i, entity);
            }
        }
        return mapLevel;
    }

    /**
     * Décompresse une chaîne de caractères encodée d'une certaine manière.
     * @param string La chaîne de caractères compressée à décompresser.
     * @return La chaîne de caractères décompressée.
     */
    private String decompressString(String string) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < string.length()) {
            char c = string.charAt(i);
            if (Character.isDigit(c)) {
                // On répète le dernier caractère autant de fois que nécessaire
                int count = Character.getNumericValue(c);
                char prevChar = sb.charAt(sb.length() - 1);
                sb.append(String.valueOf(prevChar).repeat(Math.max(0, count - 1)));
            } else {
                // On ajoute le caractère tel quel
                sb.append(c);
            }
            i++;
        }
        return sb.toString();
    }



}
