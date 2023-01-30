package net.dreamstack.sample;

import org.HdrHistogram.Histogram;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static net.dreamstack.sample.CommonStat.startHistoReader;

public class ArrayBlockingQueueApp {

    public static void main(String[] args) {
        ThroughputController controller = new ThroughputController(10_000_000);
        // create link blocking queue
        ArrayBlockingQueue<DummyEvent> queue = new ArrayBlockingQueue<>(1024 * 16);

        Histogram histogram1 = new Histogram(3);
        Histogram histogram2 = new Histogram(3);
        Histogram histogram3 = new Histogram(3);

        // queue reader
        Thread readerThread = new Thread(() -> {
            while (true) {
                try {
                    DummyEvent event = queue.poll(250, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        long oneHopDelay = System.nanoTime() - event.getCreatedTime();
                        histogram1.recordValue(oneHopDelay);
                        histogram2.recordValue(event.getEnqueueDelay());
                        histogram3.recordValue(oneHopDelay + event.getEnqueueDelay());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        startHistoReader(histogram1, histogram2, histogram3);

        // start reader
        readerThread.start();


        // publish to queue
        final AtomicLong startTime = new AtomicLong(System.nanoTime());
        while (true) {
            if (!controller.canProduce()) {
                continue;
            }
            DummyEvent dummyEvent = new DummyEvent();
            // publish to queue
            long timeNow = System.nanoTime();
            dummyEvent.setCreatedTime(timeNow);
            dummyEvent.setEnqueueDelay(timeNow - startTime.get());
            startTime.set(timeNow);

            try {
                queue.put(dummyEvent);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
