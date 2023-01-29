package net.dreamstack.sample;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThroughputController {

    private final int total;
    private final AtomicInteger counter = new AtomicInteger(0);

    public ThroughputController(int total) {
        this.total = total;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> counter.set(0), 0, 1, TimeUnit.SECONDS
        );
    }

    public boolean canProduce() {
        if (counter.get() < total) {
            counter.incrementAndGet();
            return true;
        } else {
            return false;
        }
    }

}
