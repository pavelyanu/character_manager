package org.mff;

import java.util.HashMap;
import java.util.Map;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents character sheet.
 */
public class Character implements Serializable{
    private static ArrayList<String> stats;
    private static ArrayList<String> skills;
    private static HashMap<String, String> skillToStat;
    private HashMap<String, Integer> statVals;
    private HashMap<String, Integer> statMods;
    private HashMap<String, Integer> skillMods;
    private HashMap<String, Integer> skillProfs;
    private HashMap<String, Integer> saveMods;
    private HashMap<String, Integer> saveProfs;
    private String name;
    private String player;
    private String characterClass;
    private int currentHp;
    private int maxHp;
    private int level;
    private int profLevel;

    public Character() {
        initStatic();
        statVals = new HashMap<>();
        statMods = new HashMap<>();
        skillMods = new HashMap<>();
        skillProfs = new HashMap<>();
        saveMods = new HashMap<>();
        saveProfs = new HashMap<>();
        maxHp = 6;
        currentHp = maxHp;
        level = 1;
        name = "Johnson";
        player = "John";
        characterClass = "warrior";
        initToValue(stats, statVals, 10);
        initToValue(skills, skillProfs, 0);
        initToValue(stats, saveProfs, 0);
        updateProfLevel();
        updateStatMods();
        updateSkillMods();
        updateSaveMods();
    }

    /**
     * Initializes static fields of the Character class.
     */
    public static synchronized void initStatic() {
        if (stats != null) return;
        stats = new ArrayList<>();
        skills = new ArrayList<>();
        skillToStat = new HashMap<>();
        initStats();
        initSkills();
        initSkillToStat();
    }

    /**
     * Initializes the list of possible stats of the character.
     */
    private static void initStats() {
        stats.add("strength");
        stats.add("dexterity");
        stats.add("constitution");
        stats.add("intelligence");
        stats.add("wisdom");
        stats.add("charisma");
    }

    /**
     * Initializes the list of possible skills of the character.
     */
    private static void initSkills() {
        skills.add("athletics");
        skills.add("acrobatics");
        skills.add("slight of hand");
        skills.add("stealth");
        skills.add("arcana");
        skills.add("history");
        skills.add("investigation");
        skills.add("nature");
        skills.add("religion");
        skills.add("animal handling");
        skills.add("insight");
        skills.add("medicine");
        skills.add("perception");
        skills.add("survival");
        skills.add("deception");
        skills.add("intimidation");
        skills.add("performance");
        skills.add("persuasion");
    }

    /**
     * Initializes the map from stats to skills.
     */
    private static void initSkillToStat() {
        skillToStat.put("athletics", "strength");
        skillToStat.put("acrobatics", "dexterity");
        skillToStat.put("slight of hand", "dexterity");
        skillToStat.put("stealth", "dexterity");
        skillToStat.put("arcana", "intelligence");
        skillToStat.put("history", "intelligence");
        skillToStat.put("investigation", "intelligence");
        skillToStat.put("nature", "wisdom");
        skillToStat.put("religion", "wisdom");
        skillToStat.put("animal handling", "wisdom");
        skillToStat.put("insight", "wisdom");
        skillToStat.put("medicine", "wisdom");
        skillToStat.put("perception", "wisdom");
        skillToStat.put("survival", "wisdom");
        skillToStat.put("deception", "charisma");
        skillToStat.put("intimidation", "charisma");
        skillToStat.put("performance", "charisma");
        skillToStat.put("persuasion", "charisma");
    }

    /**
     * Initializes given map to the default values using the keys provided in the keys ArrayList.
     *
     * @param keys  keys to use
     * @param map   map to initialize
     * @param value values to initialize to
     * @param <K>   type of key
     * @param <V>   type of value
     */
    private <K, V> void initToValue (ArrayList<K> keys, HashMap<K, V> map, V value) {
        for (K key : keys) {
            map.put(key, value);
        }
    }

    /**
     * Updates the modifiers of the saving throws of the character in accordance to save proficiencies
     * and relative stat values.
     */
    private void updateSaveMods() {
        for (String stat : stats) {
            int mod = statMods.get(stat);
            int prof = saveProfs.get(stat);
            if (prof == 1) mod += profLevel;
            if (prof == 2) mod += profLevel * 2;
            saveMods.put(stat, mod);
        }
    }


    /**
     * Updates the modifiers of the stats of the character in accordance to relative stat values.
     */
    private void updateStatMods() {
        for (Map.Entry<String, Integer> stat  : statVals.entrySet()) {
            statMods.put(stat.getKey(), statValToMod(stat.getValue()));
        }
    }

    /**
     * Updates the modifiers of the skills of the character in accordance to skill proficiencies
     * and relative stat values.
     */
    private void updateSkillMods() {
        for (String skill : skills) {
            int mod = statMods.get(skillToStat.get(skill));
            int prof = skillProfs.get(skill);
            if (prof == 1) mod += profLevel;
            if (prof == 2) mod += profLevel * 2;
            skillMods.put(skill, mod);
        }
    }

    /**
     * Updates proficiency level in accordance with character level.
     */
    private void updateProfLevel() {
        profLevel = (int) Math.ceil(level / 4) + 1;
    }

    /**
     * Converts value of the character stat to the stat modifier using dnd rules.
     * @param val   the value of the stat
     * @return      the stat modifier
     */
    private int statValToMod(int val) {
        return (int) Math.ceil(val / 2) - 5;
    }

    /**
     * Gets the ArrayList of stats.
     * @return  the ArrayList of skills
     */
    public ArrayList<String> getStats() {
        return new ArrayList<>(stats);
    }

    /**
     * Gets the ArrayList of skills.
     * @return  the ArrayList of skills
     */
    public ArrayList<String> getSkills() {
        return new ArrayList<>(skills);
    }

    /**
     * Gets the HashMap from stats to their values.
     * @return  the HashMap form stats to their values
     */
    public HashMap<String, Integer> getAllStatVals() {
        return new HashMap<>(statVals);
    }

    /**
     * Gets the HashMap from stats to their values.
     * @return  the HashMap form stats to their values
     */
    public HashMap<String, Integer> getAllStatMods() {
        return new HashMap<>(statMods);
    }

    /**
     * Gets the HashMap from skills to their proficiencies.
     * @return  the HashMap form skills to their proficiencies
     */
    public HashMap<String, Integer> getAllSkillProfs() {
        return new HashMap<>(skillProfs);
    }

    /**
     * Gets the HashMap from skills to their modifiers.
     * @return  the HashMap form skills to their modifiers
     */
    public HashMap<String, Integer> getAllSkillMods() {
        return new HashMap<>(skillMods);
    }

    /**
     * Gets the HashMap from saves to their proficiencies.
     * @return  the HashMap form saves ats to their proficiencies
     */
    public HashMap<String, Integer> getAllSaveProfs() {
        return new HashMap<>(saveProfs);
    }

    /**
     * Gets the HashMap from saves to their modifiers.
     * @return  the HashMap form saves to their modifiers
     */
    public HashMap<String, Integer> getAllSaveMods() {
        return new HashMap<>(saveMods);
    }

    /**
     * Gets the character's level.
     * @return  character's level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the character's max hp.
     * @return  character's max hp
     */
    public int getMaxHP() {
        return maxHp;
    }

    /**
     * Gets the character's current hp.
     * @return  character's current hp
     */
    public int getCurrentHP() {
        return currentHp;
    }

    /**
     * Gets the character's name.
     * @return  character's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the character's player.
     * @return  character's player
     */
    public String getPlayer() {
        return player;
    }

    /**
     * Gets the character's class.
     * @return  character's class
     */
    public String getCharacterClass() {
        return characterClass;
    }

    /**
     * Sets the value of the stats and calls update on all relevant fields.
     * @param stat  stat to set
     * @param value a value to set a stat to
     * @throws Exception
     */
    public void setStatValue(String stat, int value) throws Exception {
        if (!stats.contains(stat)) throw new Exception("No such stat");
        statVals.put(stat, value);
        updateStatMods();
        updateSkillMods();
        updateSaveMods();
    }

    /**
     * Sets the value of the skill and calls update on all relevant fields.
     * @param skill skill to set a proficiency of
     * @param prof  a proficiency to set to
     * @throws Exception
     */
    public void setSkillProf(String skill, int prof) throws Exception {
        if (!skill.contains(skill)) throw new Exception("No such skill");
        statVals.put(skill, prof);
        updateSkillMods();
    }


    /**
     * Sets the value of the save and calls update on all relevant fields.
     * @param save save to set a proficiency of
     * @param prof  a proficiency to set to
     * @throws Exception
     */
    public void setSaveProf(String save, int prof) throws Exception {
        if (!stats.contains(save)) throw new Exception("No such save");
        statVals.put(save, prof);
        updateSaveMods();
    }

    /**
     * Sets the value of the level and calls update on all relevant fields.
     * @param _level level to set to
     * @throws Exception
     */
    public void setLevel(int _level) {
        level = _level;
        updateProfLevel();
        updateSkillMods();
        updateSaveMods();
    }

    /**
     * Sets max HP.
     * @param _maxHP    Map HP to set
     */
    public void setMaxHP(int _maxHP) {
        maxHp = _maxHP;
    }

    /**
     * Sets the current HP.
     * @param _currentHp    current HP to set
     */
    public void setCurrentHP(int _currentHp) {
        currentHp = _currentHp;
    }

    /**
     * Subtracts damage amount from current HP.
     * @param damage    an amount to subtract from current HP
     */
    public void putDamage(int damage) {
        currentHp -= damage;
    }

    /**
     * Adds heal amount to the current HP.
     * @param heal  an amount to add to the current HP
     */
    public void putHeal(int heal) {
        currentHp += heal;
    }

    /**
     * Sets the name of the character
     * @param _name the name to set
     */
    public void setName(String _name) {
        name = _name;
    }

    /**
     * Sets the player
     * @param _player the player to set
     */
    public void setPlayer(String _player) {
        player = _player;
    }

    /**
     * Sets the character class
     * @param _characterClass the class to set
     */
    public void setCharacterClass(String _characterClass) {
        characterClass = _characterClass;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name + '\n');
        sb.append("LEVEL: " + level + '\n');
        sb.append("CLASS: " + characterClass + '\n');
        sb.append("HP: " + currentHp + "/" + maxHp + '\n');
        sb.append("---STATS---\n");
        for (Map.Entry<String, Integer> stat : statMods.entrySet()) {
            int s = stat.getValue();
            sb.append(
                    stat.getKey()
                            + " "
                            + statVals.get(stat.getKey())
                            + ":"
                            + (s >= 0 ? "+" + s : + s)
                            + '\n'
            );
        }
        sb.append("---SAVES---\n");
        for (Map.Entry<String, Integer> save : saveMods.entrySet()) {
            int s = save.getValue();
            sb.append(
                    save.getKey()
                            + " "
                            + saveProfs.get(save.getKey())
                            + ":"
                            + (s >= 0 ? "+" + s : + s)
                            + '\n'
            );
        }
        sb.append("---SKILLS---\n");
        for (Map.Entry<String, Integer> skill : skillMods.entrySet()) {
            int s = skill.getValue();
            sb.append(
                    skill.getKey()
                            + " "
                            + skillProfs.get(skill.getKey())
                            + ":"
                            + (s >= 0 ? "+" + s : + s)
                            + '\n'
            );
        }
        return sb.toString();
    }
}
