package org.mff;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An interface with the persistent data of the application.
 */
public class Database {

    private static Database instance;
    private static String compain;
    private static Path compainPath;
    private static Path metaDataPath;
    private static Path compainMetaDataPath;
    private static final String relativePathToData = "Data";
    private static final String relativePathToMetaData = "MetaData";
    private static final String metaDataFileName = "meta";
    private static Path dataPath;
    private static Path userDirectory;
    private static ObjectMapper mapper;

    private Database(String _compain) {
        userDirectory = Paths.get("").toAbsolutePath();
        dataPath = Paths.get(userDirectory.toString(), relativePathToData);
        metaDataPath = Paths.get(userDirectory.toString(), relativePathToMetaData);
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    /**
     * Creates the instance of the database if the is none.
     * @param _compain  The name of compain
     * @return          the instance of the database
     */
    public static synchronized Database getInstance(String _compain) throws IOException {
        if (instance == null) {
            instance = new Database(_compain);
        }
        setCompain(_compain);
        return instance;
    }

    /**
     * Sets the compain of for the database.
     * @throws IOException
     * @param _compain  the name of the compain to set
     */
    private static void setCompain(String _compain) throws IOException {
        compain = _compain;
        compainPath = Paths.get(userDirectory.toString(), relativePathToData, compain);
        Path compainMetaDataDir = Paths.get(metaDataPath.toString(), compain);
        compainMetaDataPath = Paths.get(metaDataPath.toString(), compain, metaDataFileName);
        if (!Files.isDirectory(compainPath)) {
            Files.createDirectories(compainPath);
        }
        if (!Files.isDirectory(compainMetaDataDir)) {
            Files.createDirectories(compainMetaDataDir);
        }
    }

    /**
     * Writes the character to the appropriate file.
     * @throws IOException
     * @param character the character to write
     */
    public static synchronized void writeCharacter(Character character) throws IOException {
        Path characterPath = getPathToCharacter(character.getName());
        mapper.writeValue(new File(characterPath.toUri()), character);
    }

    /**
     * Reads the character form the file.
     * @param name  the name of the character to read
     * @throws IOException
     * @return      the character
     */
    public static Character readCharacter(String name) throws IOException {
        Path characterPath = getPathToCharacter(name);
        String characterJson = Files.readAllLines(characterPath).get(0);
        Character character = mapper.readValue(characterJson, Character.class);
        Character.initStatic();
        return character;
    }

    /**
     * Gets the path to the character.
     * @param name the name of the character to get the path to
     * @return
     */
    private static Path getPathToCharacter(String name) {
        Path characterPath = Paths.get(compainPath.toString(), name.replaceAll(" ", ""));
        return characterPath;
    }

    /**
     * Gets the ArrayList of the existing compains.
     * @return  the ArrayList of compains
     * @throws IOException
     */
    public static ArrayList<String> getExistingCompains() throws IOException {
        ArrayList<String> result = null;
        try (Stream<Path> walk = Files.walk(dataPath)) {
            result = new ArrayList<String>(
                    walk
                            .filter(Files::isDirectory)
                            .map(Path::getFileName)
                            .map(Path::toString)
                            .collect(Collectors.toList()));

        } catch (Exception e) {
            throw e;
        }
        return result;
    }
    /**
     * Gets the ArrayList of the character names in the current compain.
     * @return  the ArrayList of character names
     * @throws IOException
     */
    public static ArrayList<String> getExistingCharacters() throws IOException {
        ArrayList<String> result = null;
        try (Stream<Path> walk = Files.walk(compainPath)) {
            result = new ArrayList<String>(
                    walk
                            .filter(Files::isRegularFile)
                            .map(Path::getFileName)
                            .map(Path::toString)
                            .collect(Collectors.toList()));

        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    /**
     * Gets the HashMap from players to their characters.
     * @return  the HashMap from players to their characters
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, String> ReadCompainMetadata() throws JsonParseException, JsonMappingException, IOException {
        if (!Files.exists(compainMetaDataPath)) return new HashMap<>();
        String metaJson = Files.readAllLines(compainMetaDataPath).get(0);
        HashMap<String, String> map = new HashMap<>();
        map = mapper.readValue(metaJson, map.getClass());
        return map;
    }

    /**
     * Writes the HashMap from players to their characters as the compain's metadata.
     * @param map   the HashMap to write
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static void writeCompainMetadata(HashMap<String, String> map) throws JsonParseException, JsonMappingException, IOException {
        mapper.writeValue(new File(compainMetaDataPath.toUri()), map);
    }
}
