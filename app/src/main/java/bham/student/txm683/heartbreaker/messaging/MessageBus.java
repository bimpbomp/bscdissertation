package bham.student.txm683.heartbreaker.messaging;

import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

public class MessageBus {
    private String TAG = "hb::MessageBus";
    private ConcurrentHashMap<String, Mailbox> systemMailboxes;
    public int count = 0;

    public MessageBus(){
        this.systemMailboxes = new ConcurrentHashMap<>();
    }

    public boolean postMessage(Message message){

        if (this.systemMailboxes.containsKey(message.receiver)){
            this.systemMailboxes.get(message.receiver).deliverToMailbox(message);
            count += 1;
            return true;
        }
        return false;
    }

    public Mailbox registerMailbox(String systemName){
        if (!this.systemMailboxes.containsKey(systemName)){
            Mailbox mailbox = new Mailbox();
            this.systemMailboxes.put(systemName, mailbox);
            return mailbox;
        }
        return this.systemMailboxes.get(systemName);
    }

    public void removeMailbox(String systemName){
        this.systemMailboxes.remove(systemName);
    }

    public void removeAllMailboxes(){
        this.systemMailboxes.clear();
    }

}