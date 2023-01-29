package net.dreamstack.sample;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.HdrHistogram.Histogram;

import java.util.concurrent.atomic.AtomicLong;

import static net.dreamstack.sample.CommonStat.startHistoReader;

/**
 * Hello world!
 */
public class DisruptorApp {
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

        startHistoReader(histogram1, histogram2, histogram3);

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
