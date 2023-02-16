package net.dreamstack.sample.quickfix;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.MessageFactory;
import quickfix.NoopStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class InitiatorStarter {

    public static void main(String[] args) throws IOException, ConfigError, InterruptedException {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = InitiatorStarter.class.getResourceAsStream("initiator.cfg");
        } else if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + InitiatorStarter.class.getName() + " [configFile].");
            return;
        }

        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();

        MessageFactory messageFactory = new DefaultMessageFactory();

        SocketInitiator initiator = new SocketInitiator(new InitiatorSampleApp(), new NoopStoreFactory(), settings, new SLF4JLogFactory(settings),
                messageFactory);
        initiator.start();

        Thread.currentThread().join();
    }

}
