package bham.student.txm683.heartbreaker.system;

import bham.student.txm683.heartbreaker.messaging.Mailbox;
import bham.student.txm683.heartbreaker.messaging.MessageBus;

public abstract class System implements Runnable{
    private final String TAG;

    private Mailbox mailbox;
    private MessageBus messageBus;
    public final String NAME;

    public System(String name, MessageBus messageBus){
        this.NAME = name;
        this.TAG = "hb::" + name;
        this.messageBus = messageBus;

        this.mailbox = this.messageBus.registerMailbox(this.NAME);
    }

    public abstract void run();
}