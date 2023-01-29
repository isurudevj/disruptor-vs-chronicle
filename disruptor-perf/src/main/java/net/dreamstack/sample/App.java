package net.dreamstack.sample;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.HdrHistogram.Histogram;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Disruptor<DummyEvent> disruptor = new Disruptor<>(DummyEvent::new,
                1024 * 16,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());


        Histogram histogram1 = new Histogram(3);
        Histogram histogram2 = new Histogram(3);
        Histogram histogram3 = new Histogram(3);

        disruptor.handleEventsWith(new EventHandler<DummyEvent>() {
            @Override
            public void onEvent(DummyEvent event, long sequence, boolean endOfBatch) throws Exception {
                long oneHopDelay = System.nanoTime() - event.getCreatedTime();
                histogram1.recordValue(oneHopDelay);
                histogram2.recordValue(event.getEnqueueDelay());
                histogram3.recordValue(oneHopDelay  + event.getEnqueueDelay());
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.printf("Total count = %d\n", histogram1.getTotalCount());
            System.out.printf("1 Hop    Delay = 99.99%% = %d us\n", Math.round(histogram1.getValueAtPercentile(99.99) / 1_000.0f));
            System.out.printf("Enqueue  Delay = 99.99%% = %d us\n", Math.round(histogram2.getValueAtPercentile(99.99) / 1_000.0f));
            System.out.printf("Total  Delay = 99.99%% = %d us\n", Math.round(histogram3.getValueAtPercentile(99.99) / 1_000.0f));

            histogram1.reset();
            histogram2.reset();
            histogram3.reset();
        }, 10, 10, TimeUnit.SECONDS);

        disruptor.start();

        final AtomicLong startTime = new AtomicLong(System.nanoTime());
        while (true) {
            disruptor.getRingBuffer().publishEvent((event, sequence) -> {
                long timeNow = System.nanoTime();
                event.setCreatedTime(timeNow);
                event.setEnqueueDelay(timeNow - startTime.get());
                startTime.set(timeNow);
            });
        }

    }


}
