package bham.student.txm683.heartbreaker.messaging;

import java.util.Random;

public class MessagingTest extends Thread {
    private static String TAG = "hb::MessagingTest";
    private static Random rand = new Random();
    private static int numberOfSystems = 3;
    private static System[] systems = new System[numberOfSystems+1];
    private static MessageBus messageBus = new MessageBus();

    public static final int numberOfMessages = 20;

    public void run(){

        /*//initiate systems
        for (int i = 0; i < numberOfSystems; i++){
            String systemName = "system-" + i;
            systems[i] = new System(systemName, messageBus);
        }

        //start systems
        for (int i = 0; i < numberOfSystems; i++){
            systems[i].start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){

        }

        //count sent messages
        int count = 0;
        for (int i= 0; i < numberOfSystems; i++){
            count += systems[i].receivedMessageTotal;
        }*/
    }

    private static String getRandomSystem(){
        return systems[rand.nextInt(numberOfSystems)].NAME;
    }

    public static Message generateRandomMessage(String sender, int dataNum){
        return new Message(sender, getRandomSystem(), false, "type", "data " + dataNum);
    }
}