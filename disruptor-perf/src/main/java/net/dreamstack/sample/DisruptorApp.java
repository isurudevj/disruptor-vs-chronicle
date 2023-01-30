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
        ThroughputController controller = new ThroughputController(10_000_000);

        Disruptor<DummyEventContext> disruptor = new Disruptor<>(DummyEventContext::new,
                1024 * 16,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());


        Histogram histogram1 = new Histogram(3);
        Histogram histogram2 = new Histogram(3);
        Histogram histogram3 = new Histogram(3);

        disruptor.handleEventsWith(new EventHandler<DummyEventContext>() {
            @Override
            public void onEvent(DummyEventContext ctx, long sequence, boolean endOfBatch) throws Exception {
                DummyEvent event = ctx.getDummyEvent();
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
            if (!controller.canProduce()) {
                continue;
            }
            disruptor.getRingBuffer().publishEvent((ctx, sequence) -> {
                DummyEvent event = new DummyEvent();
                ctx.setDummyEvent(event);
                long timeNow = System.nanoTime();
                event.setCreatedTime(timeNow);
                event.setEnqueueDelay(timeNow - startTime.get());
                startTime.set(timeNow);
            });
        }

    }


}
