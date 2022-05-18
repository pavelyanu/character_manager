package org.mff;
import java.io.Serializable;

/**
 * Represents the message. Used to communicate between server and client.
 */
public class Message implements Serializable{
    public MessageType type;
    public String payload;
    public Message(MessageType _type, String _payload) {
        type = _type;
        payload = _payload;
    }
}
