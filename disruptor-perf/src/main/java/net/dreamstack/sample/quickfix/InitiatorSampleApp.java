package net.dreamstack.sample.quickfix;

import org.HdrHistogram.Histogram;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.TransactTime;
import quickfix.fix44.MessageCracker;
import quickfix.fix44.NewOrderSingle;

import java.time.ZoneOffset;

public class InitiatorSampleApp extends MessageCracker implements Application {

    Histogram histogram = new Histogram(3);

    @Override
    public void onCreate(SessionID sessionId) {
        System.out.println("Created " + sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        System.out.println("Logon " + sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        System.out.println("Logout " + sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        System.out.println("To Admin message " + message.toString());
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("From Admin message " + message.toString());
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

    }

    @Override
    public void onMessage(NewOrderSingle message, SessionID sessionID) throws FieldNotFound {
        System.out.println("Got a order " + message);
        TransactTime transactTime = new TransactTime();
        message.getField(transactTime);

        long delta = System.currentTimeMillis() - transactTime.getValue().toEpochSecond(ZoneOffset.UTC);
        histogram.recordValue(delta);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        try {
            crack(message, sessionId);
        } catch (UnsupportedMessageType e) {
            throw new RuntimeException(e);
        } catch (FieldNotFound e) {
            throw new RuntimeException(e);
        } catch (IncorrectTagValue e) {
            throw new RuntimeException(e);
        }
    }
}
