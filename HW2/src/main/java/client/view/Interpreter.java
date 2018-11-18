package client.view;

import client.net.Connection;
import client.net.Listener;

import java.io.IOException;
import java.util.Scanner;

public class Interpreter implements Runnable{

    private static final String PROMPT = "~> ";
    private final Scanner userIn = new Scanner(System.in);
    private boolean active = false;
    private Connection connection;
    private final StdOut stdOut = new StdOut();
    private HangManHandler hangManHandler = new HangManHandler();

    public void start() {

        active = true;
        connection = new Connection();
        new Thread(this).start();

    }

    public void run() {

        System.out.println(
                "*\n" +
                "*\n" +
                "*\n" +
                "type \"connect\" followed by the server ip address and port number to connect to the hangman server.\n" +
                "e.g. \"connect 127.0.0.1 8080\" \n" +
                "*\n" +
                "*\n" +
                "*" );
        while(active){
            stdOut.print(PROMPT);
            Input input = new Input(userIn.nextLine());

            if(input.getCommand().equals("CONNECT")) {
                connection.setListener(new ConsoleOutput());
                connection.connect(input.getParamOne(), Integer.valueOf(input.getParamTwo()));
            }
            else if( input.getCommand().equals("START")){
                connection.newGame(input.getCommand());
            }
            else if( input.getCommand().equals("GUESS")){
                connection.guess(input.getCommand(), input.getParamOne());
            }
            else if( input.getCommand().equals("HELP")){
                stdOut.println(hangManHandler.getInfo());
            }
            else if( input.getCommand().equals("QUIT")){
                try {
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ConsoleOutput implements Listener {
        @Override
        public void handleMsg(String msg) {
            String inputArray[] = msg.split(" ");
            if(inputArray[0].equals("START")){
                msg = hangManHandler.newGame(msg);
            }
            else if(inputArray[0].equals("GUESS")){
                msg = hangManHandler.guess(msg);
            }
            else if(inputArray[0].equals("INVALID")){
                if(inputArray[1].equals("NOSTART")){
                    msg = "Use command \"start\" to start a game before guessing.";
                }
                if(inputArray[1].equals("NOGUESS")){
                    msg = "Use command \"guess *\" where * is the letter or word you want to guess.";
                }
            }
            else if(inputArray[0].equals("CONNECTED")){
                msg = hangManHandler.connected();
            }
            else if(inputArray[0].equals("DISCONNECTED")){
                msg = "Disconnected from server";
            }

            stdOut.println(msg);
            stdOut.print(PROMPT);
        }

    }


}
