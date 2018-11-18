package server.net;

import server.model.HangmanBot;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class HangmanServer {

    private static final int LINGER_TIME = 5000;

    public void serve() {
        try {
            int portNo = 8080;
            Selector selector = Selector.open();
            ServerSocketChannel listeningServerSocketChannel = ServerSocketChannel.open();
            listeningServerSocketChannel.configureBlocking(false);
            listeningServerSocketChannel.bind(new InetSocketAddress(portNo));
            listeningServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverSocketChannel.accept();
                        clientChannel.configureBlocking(false);
                        HangmanBot hangmanBot = new HangmanBot();
                        ClientHandler clientHandler = new ClientHandler(clientChannel, hangmanBot);
                        clientChannel.register(selector, SelectionKey.OP_READ, new Client(clientHandler));
                        clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
                    }
                    else if (key.isReadable()) {
                        Client client = (Client) key.attachment();
                        client.handler.messageIn();
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    else if (key.isWritable()) {
                        Client client = (Client) key.attachment();
                        client.send();
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Server failure.");
        }
    }

    private class Client {
        private final ClientHandler handler;
        private final Queue<ByteBuffer> messageOutbox = new ArrayDeque<>();

        private Client(ClientHandler handler) {
            this.handler = handler;
        }

        private void send() {
            ByteBuffer msg = null;
            synchronized (messageOutbox) {
                while ((msg = messageOutbox.peek()) != null) {
                    handler.sendMessage(msg);
                    messageOutbox.remove();
                }
            }
        }


    }


}
