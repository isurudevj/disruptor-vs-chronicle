package net.dreamstack.sample;

import com.lmax.disruptor.EventFactory;

public class DummyEventFactory implements EventFactory<DummyEvent> {
    @Override
    public DummyEvent newInstance() {
        return new DummyEvent();
    }
}
