package bham.student.txm683.heartbreaker.messaging;

public class Message {
    public final String sender;
    public final String receiver;
    public final boolean important;
    public final String type;
    public final String data;

    public Message(String sender, String receiver, boolean important, String type, String data) {
        this.sender = sender;
        this.receiver = receiver;
        this.important = important;
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", important=" + important +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}