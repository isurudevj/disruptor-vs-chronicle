package net.dreamstack.sample;

import lombok.Data;

@Data
public class DummyEvent {
    private long id;
    private long createdTime;
    private long enqueueDelay;
}
