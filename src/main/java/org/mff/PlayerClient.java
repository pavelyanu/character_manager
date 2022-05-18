package org.mff;
import java.io.Console;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents player client. Governs the client server communication.
 */
public class PlayerClient {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Console console;
    private String playerName;
    private ObjectMapper mapper;

    /**
     * Starts the connection with the server.
     * @param ip    IP to connect to
     * @param port  port to connect to
     */
    public void startConnection(String ip, int port) {
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        try {
            clientSocket = new Socket(ip, port);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Did't manage to connect to host.");
            System.out.println("Trying again...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return;
            }
            startConnection(ip, port);
        }
    }

    /**
     * Starts the client and tries to connect to 127.0.0.1:6666.
     * @param args
     */
    public static void run(String[] args) {
        PlayerClient client = new PlayerClient();
        client.console = System.console();
        Pattern exitPattern = Pattern.compile("(exit)|(quit)|(q)", Pattern.CASE_INSENSITIVE);
        Matcher exitMatcher;
        client.startConnection("127.0.0.1", 6666);
        try {
            client.authenticate();
            client.printHelp();
            while (true) {
                String input = client.console.readLine().toLowerCase();
                exitMatcher = exitPattern.matcher(input);
                if (exitMatcher.matches()) {
                    client.out.writeObject(new Message(MessageType.CLOSE, ""));
                    client.stopConnection();
                    return;
                }
                client.processMessage(input);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.out.println("An unexpected error occurred. Exiting.");
            System.exit(1);
        }
    }

    /**
     * Processes the clients input.
     * @param input the input to process
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void processMessage(String input) throws ClassNotFoundException, IOException {
        System.out.println("---------------------------------");
        switch (input) {
            case "auth" -> authenticate();
            case "pick" -> processPick();
            case "view" -> processView();
            case "set" -> processSet();
            case "help" -> printHelp();
            default -> System.out.println("Unknown command");
        }
    }

    /**
     * Processes the set command. Sends the SET request, gets the character from the server and
     * continuously processes the commands to set fields of the character. Sends the potentially changed character
     * back to the server.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void processSet() throws IOException, ClassNotFoundException {
        out.writeObject(new Message(MessageType.SET, ""));
        Message response = (Message) in.readObject();
        Character character;
        if (handleIfError(response))
            return;
        else {
            character = readCharacter(((Message) in.readObject()).payload);
            System.out.println("Character received successfully");
        }
        System.out.println("Enter:");
        System.out.println("stat - to set stat, save - to set save proficiency, skill - to set skill proficiency");
        System.out.println("class - to set class, level - to set level, max health - to set max health");
        System.out.println(
                "health - to set current health, heal - to add to current health, harm - to subtract from current health");
        System.out.println("Enter 'done' to stop setting values and save the result");
        System.out.println("Enter 'undo' to stop setting values and discard the result");
        outer:
        while (true) {
            String input = console.readLine().toLowerCase();
            switch (input) {
                case "stat" -> {
                    System.out.println("Enter stat");
                    String stat = console.readLine().toLowerCase();
                    System.out.println("Enter value");
                    int value = Integer.parseInt(console.readLine().toLowerCase());
                    try {
                        character.setStatValue(stat, value);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        continue outer;
                    }
                }
                case "save" -> {
                    System.out.println("Enter save");
                    String save = console.readLine().toLowerCase();
                    System.out.println("Enter value");
                    int value = Integer.parseInt(console.readLine().toLowerCase());
                    try {
                        character.setSaveProf(save, value);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        continue outer;
                    }
                }
                case "skill" -> {
                    System.out.println("Enter skill");
                    String skill = console.readLine().toLowerCase();
                    System.out.println("Enter value");
                    int value = Integer.parseInt(console.readLine().toLowerCase());
                    try {
                        character.setSkillProf(skill, value);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        continue outer;
                    }
                }
                case "class" -> {
                    System.out.println("Enter class");
                    String characterClass = console.readLine().toLowerCase();
                    character.setCharacterClass(characterClass);
                }
                case "level" -> {
                    System.out.println("Enter level");
                    int level = Integer.parseInt(console.readLine().toLowerCase());
                    character.setLevel(level);
                }
                case "max health" -> {
                    System.out.println("Enter new max health");
                    int value = Integer.parseInt(console.readLine().toLowerCase());
                    character.setMaxHP(value);
                }
                case "health" -> {
                    System.out.println("Enter new current health");
                    int value = Integer.parseInt(console.readLine().toLowerCase());
                    character.setCurrentHP(value);
                }
                case "heal" -> {
                    System.out.println("Enter amount to heal");
                    int value = Integer.parseInt(console.readLine().toLowerCase());
                    character.putHeal(value);
                }
                case "harm" -> {
                    System.out.println("Enter amount to harm");
                    int value = Integer.parseInt(console.readLine().toLowerCase());
                    character.putDamage(value);
                }
                case "done" -> {
                    out.writeObject(new Message(MessageType.OK, ""));
                    out.writeObject(new Message(MessageType.MESSAGE, mapper.writeValueAsString(character)));
                    break outer;
                }
                case "undo" -> {
                    out.writeObject(new Message(MessageType.DISCARD, ""));
                    break outer;
                }
                default -> System.out.println("Unknown command");
            }
        }

    }

    /**
     * Processes the View command. Sends VIEW request and handles the response.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void processView() throws IOException, ClassNotFoundException {
        Message response;
        out.writeObject(new Message(MessageType.VIEW, ""));
        response = (Message) in.readObject();
        if (handleIfError(response))
            return;
        System.out.println(response.payload);
    }

    /**
     * Processes the Pick command. Sends LIST request, handles the response, sends the Message with the picked character.
     * Handles the response.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void processPick() throws IOException, ClassNotFoundException {
        Message response;
        out.writeObject(new Message(MessageType.LIST, ""));
        response = (Message) in.readObject();
        if (handleIfError(response))
            return;
        System.out.println(response.payload);
        System.out.println(
                "Enter the name of one of the characters above to choose them or a new name, to create a new character.");
        String name = console.readLine();
        out.writeObject(new Message(MessageType.PICK, name));
        response = (Message) in.readObject();
        if (handleIfError(response))
            return;
        if (response.type == MessageType.OK) {
            System.out.println("You have been assigned a character " + name);
        }
    }

    /**
     * Handles authentication on the client side. Sends AUTH request and handles the response.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void authenticate() throws IOException, ClassNotFoundException {
        System.out.println("Enter your player name...");
        playerName = console.readLine();
        Message response;
        out.writeObject(new Message(MessageType.AUTH, playerName));
        response = (Message) in.readObject();
        if (handleIfError(response))
            return;
        else if (response.type == MessageType.OK) {
            System.out.println("Authenticated successfully");
            return;
        } else if (response.type == MessageType.MESSAGE) {
            System.out.println("Authenticated successfully");
            System.out.println(response.payload);
        }
    }

    /**
     * Stops the connection with the server.
     * @throws IOException
     */
    private void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    /**
     * Prints the help message.
     */
    private void printHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("The following commands are available:\n");
        sb.append("auth - re-authenticate\n");
        sb.append("pick - pick a character\n");
        sb.append("view - view a character sheet\n");
        sb.append("set - set a value in a character sheet\n");
        sb.append("help - print this message\n");
        System.out.println(sb.toString());
    }

    /**
     * Checks if the message is of ERROR type. If so, handles the error.
     * @param m message to handle
     * @return  true if the message is and ERROR, false otherwise
     */
    private boolean handleIfError(Message m) {
        if (m.type == MessageType.ERROR) {
            System.out.println("An error occurred: " + m.payload);
            return true;
        }
        return false;
    }

    /**
     * Reads the character from the json string.
     * @param json  the string to read a character from
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private Character readCharacter(String json) throws JsonParseException, JsonMappingException, IOException {
        Character character = mapper.readValue(json, Character.class);
        Character.initStatic();
        return character;
    }
}
