package net.dreamstack.sample.quickfix;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.LocateReqd;
import quickfix.field.MsgType;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.MessageCracker;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class AcceptorSampleApp extends MessageCracker implements Application {

    private final AtomicBoolean startFiring = new AtomicBoolean(false);

    @Override
    public void onCreate(SessionID sessionId) {

    }

    @Override
    public void onLogon(SessionID sessionId) {
        startSendingOrders(sessionId);
    }

    private void startSendingOrders(SessionID sessionId) {
        try {
            quickfix.fix44.NewOrderSingle newOrderSingle = new quickfix.fix44.NewOrderSingle(
                    new ClOrdID(UUID.randomUUID().toString()), new Side(Side.BUY),
                    new TransactTime(), new OrdType(OrdType.LIMIT));
            newOrderSingle.set(new OrderQty(100_000));
            newOrderSingle.set(new Symbol("BNA"));
            newOrderSingle.set(new HandlInst('1'));
            Session.sendToTarget(newOrderSingle, sessionId);
            System.out.println("Order sent " + newOrderSingle.toString());
        } catch (SessionNotFound e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLogout(SessionID sessionId) {

    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {

    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);
            if (MsgType.HEARTBEAT.equals(msgType) && startFiring.compareAndSet(false, true)) {
                System.out.println("Start firing rates.");
                startSendingOrders(sessionId);
            }
        } catch (Exception e) {
            System.out.println("35 field not found in message " + message);
            e.printStackTrace();
        }

    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

    }


}
