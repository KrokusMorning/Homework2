package server.net;

import server.model.HangmanBot;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;

public class ClientHandler implements Runnable{

    private final SocketChannel socketChannel;
    private final ByteBuffer clientMessageBuffer = ByteBuffer.allocateDirect(1000);
    private final Queue<String> messageInbox = new ArrayDeque<>();
    private HangmanBot hangmanBot;

    ClientHandler(SocketChannel socketChannel, HangmanBot hangmanBot) {
        this.socketChannel = socketChannel;
        this.hangmanBot = hangmanBot;
    }

    @Override
    public void run() {
        

        while (!messageInbox.isEmpty()) {
                String inputArray[] = messageInbox.poll().split(" ");

                if(inputArray[0].equals("START")){
                    String newGameString = hangmanBot.newGame();
                    sendMessage(ByteBuffer.wrap(newGameString.getBytes()));
                }
                else if(inputArray[0].equals("GUESS")){
                    if(inputArray.length > 1){
                        String guessResult = hangmanBot.guess(inputArray[1]);
                        sendMessage(ByteBuffer.wrap(guessResult.getBytes()));
                    }
                }
                else if(inputArray[0].equals("DISCONNECT")){
                    disconnectClient();
                    break;
                }
        }
    }

    void sendMessage(ByteBuffer msg) {
        try {
            socketChannel.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectClient() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void messageIn() {
        clientMessageBuffer.clear();
        try {
            socketChannel.read(clientMessageBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientMessageBuffer.flip();
        byte[] bytes = new byte[clientMessageBuffer.remaining()];
        clientMessageBuffer.get(bytes);
        String command = new String(bytes);
        messageInbox.add(command);
        ForkJoinPool.commonPool().execute(this);
    }
}
