package bham.student.txm683.heartbreaker.messaging;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Mailbox {
    private ConcurrentLinkedQueue<Message> mailbox;

    public Mailbox(){
        this.mailbox = new ConcurrentLinkedQueue<>();
    }

    public Message pop(){
        return this.mailbox.poll();
    }

    public Message peek(){
        return this.mailbox.peek();
    }

    public void deliverToMailbox(Message message){
        this.mailbox.add(message);
    }

    public boolean hasMessages(){
        return this.mailbox.size() > 0;
    }

    public int size(){
        return this.mailbox.size();
    }
}
