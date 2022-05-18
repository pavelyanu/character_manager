package org.mff;
import java.io.Console;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents the server. Governs the allocation of new threads for the clients as well as server client communication.
 */
public class Server {
    private ServerSocket serverSocket;
    private Console console;
    private boolean stopped;

    /**
     * Creates and runs the server on the port 6666.
     * @param args
     */
    public static void run(String[] args) {
        Server server = new Server();
        server.run(6666);
    }

    /**
     * Runs the server on the given port.
     * @param port  the port to run the server on
     */
    public void run(int port) {
        stopped = false;
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        console = System.console();
        System.out.println("Enter compain name");
        String compain = console.readLine();
        try {
            Database.getInstance(compain);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (!stopped)
            try {
                new ClinentHandler(serverSocket.accept()).start();
            } catch (IOException e) {}
    }

    /**
     * Stops the server.
     * @throws IOException
     */
    public void stop() throws IOException {
        stopped = true;
        serverSocket.close();
    }

    /**
     * Class responsible for running all the communication with the client in the thread.
     */
    private static class ClinentHandler extends Thread {
        private Socket clientSocket;
        private String clientName;
        private String characterName;
        private ObjectMapper mapper;

        public ClinentHandler(Socket socket) {
            this.clientSocket = socket;
        }

        /**
         * Runs the ClientHandler. Creates input and output streams, reads for input and processes it.
         */
        public void run() {
            mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                while (true) {
                    try {
                        Object o = in.readObject();
                        if (o instanceof Message) {
                            Message message = (Message) o;
                            switch (message.type) {
                                case AUTH -> processAuth(in, out, message);
                                case LIST -> processList(in, out, message);
                                case PICK -> processPick(in, out, message);
                                case VIEW -> processView(in, out, message);
                                case SET -> processSet(in, out, message);
                                case CLOSE -> {
                                    try {
                                        clientSocket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return;
                                }
                                default -> throw new IllegalArgumentException("Unknown command type");
                            }
                        } else {
                            System.out.println("Unexpected object received");
                        }
                    } catch (ClassNotFoundException ex) {
                        System.out.println("Unknown object received");
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

        /**
         * Processes AUTH message. Read the metadata. If player is in the metadata, they get assigned appropriate
         * character.
         * @param in            the input stream
         * @param out           the output stream
         * @param message       the message to process
         * @throws IOException
         */
        public void processAuth(ObjectInputStream in, ObjectOutputStream out, Message message) throws IOException {
            clientName = message.payload;
            HashMap<String, String> map;
            try {
                map = Database.ReadCompainMetadata();
            } catch (IOException e) {
                e.printStackTrace();
                out.writeObject(new Message(MessageType.ERROR, "Unable to read compain metadata"));
                return;
            }
            characterName = null;
            if (map.containsKey(clientName)) {
                characterName = map.get(clientName);
                out.writeObject(new Message(MessageType.MESSAGE, "Found your previous character: " + characterName));
            } else {
                out.writeObject(new Message(MessageType.OK, ""));
            }
        }

        /**
         * Processes LIST message. Lists the available characters.
         * @param in            the input stream
         * @param out           the output stream
         * @param message       the message to process
         * @throws IOException
         */
        public void processList(ObjectInputStream in, ObjectOutputStream out, Message message) throws IOException {
            StringBuilder sb = new StringBuilder();
            ArrayList<String> characters = Database.getExistingCharacters();
            HashMap<String, String> map;
            try {
                map = Database.ReadCompainMetadata();
            } catch (IOException e) {
                e.printStackTrace();
                out.writeObject(new Message(MessageType.ERROR, "Unable to read compain metadata"));
                return;
            }
            for (var character : characters) {
                if (map.containsValue(character))
                    continue;
                sb.append(character);
                sb.append('\n');
            }
            out.writeObject(new Message(MessageType.MESSAGE, sb.toString()));
        }

        /**
         * Processes PICK message. Gets existing characters, reads the metadata. If the chosen character already has
         * a players assigned to it or if the current player is already assigned to it sends an ERROR message.
         * Otherwise, assigns the character to the player.
         * @param in            the input stream
         * @param out           the output stream
         * @param message       the message to process
         * @throws IOException
         */
        public void processPick(ObjectInputStream in, ObjectOutputStream out, Message message) throws IOException {
            String characterName = message.payload;
            HashMap<String, String> map;
            ArrayList<String> characters = Database.getExistingCharacters();
            try {
                map = Database.ReadCompainMetadata();
            } catch (IOException e) {
                e.printStackTrace();
                out.writeObject(new Message(MessageType.ERROR, "Unable to read compain metadata"));
                return;
            }
            if (map.get(clientName) == characterName) {
                out.writeObject(new Message(MessageType.ERROR, "You are already assigned to this character"));
                return;
            }
            if (map.containsValue(characterName)) {
                out.writeObject(
                        new Message(MessageType.ERROR, "Can't pick this character. It is assigned to other player"));
                return;
            }
            map.remove(clientName);
            if (!characters.contains(characterName)) {
                Character character = new Character();
                character.setName(characterName);
                Database.writeCharacter(character);
            }
            this.characterName = characterName;
            map.put(clientName, characterName);
            Database.writeCompainMetadata(map);
            out.writeObject(new Message(MessageType.OK, ""));
        }

        /**
         * Processes VIEW message. Reads the character from the database. Sends the character's string representation.
         * @param in            the input stream
         * @param out           the output stream
         * @param message       the message to process
         * @throws IOException
         */
        public void processView(ObjectInputStream in, ObjectOutputStream out, Message message) throws IOException {
            if (characterName == null) {
                out.writeObject(new Message(MessageType.ERROR, "You have not picked a character yet."));
                return;
            }
            Character character = Database.readCharacter(characterName);
            out.writeObject(new Message(MessageType.MESSAGE, character.toString()));
        }

        /**
         * Processes SET message. Reads the character from the database. Sends the character and gets the character back.
         * @param in            the input stream
         * @param out           the output stream
         * @param message       the message to process
         * @throws IOException
         */
        public void processSet(ObjectInputStream in, ObjectOutputStream out, Message message)
                throws IOException, ClassNotFoundException {
            if (characterName == null) {
                out.writeObject(new Message(MessageType.ERROR, "You have not picked a character yet."));
                return;
            } else {
                out.writeObject(new Message(MessageType.OK, ""));
            }
            Character character = Database.readCharacter(characterName);

            String jsonString = mapper.writeValueAsString(character);
            out.writeObject(new Message(MessageType.MESSAGE, jsonString));
            Message response = (Message) in.readObject();
            if (response.type == MessageType.DISCARD)
                return;
            character = (Character) mapper.readValue(((Message) in.readObject()).payload, Character.class);
            Database.writeCharacter(character);
        }
    }
}
