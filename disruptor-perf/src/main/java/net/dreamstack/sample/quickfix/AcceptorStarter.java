package net.dreamstack.sample.quickfix;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.MessageFactory;
import quickfix.NoopStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AcceptorStarter {

    public static void main(String[] args) throws IOException, ConfigError, InterruptedException {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = AcceptorStarter.class.getResourceAsStream("acceptor.cfg");
        } else if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + AcceptorStarter.class.getName() + " [configFile].");
            return;
        }

        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();

        MessageFactory messageFactory = new DefaultMessageFactory();

        SocketAcceptor acceptor = new SocketAcceptor(new AcceptorSampleApp(), new NoopStoreFactory(), settings, new SLF4JLogFactory(settings),
                messageFactory);
        acceptor.start();

        Thread.currentThread().join();
    }

}
