package net.dreamstack.sample;

import net.openhft.chronicle.bytes.MethodReader;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.HdrHistogram.Histogram;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import static net.dreamstack.sample.CommonStat.startHistoReader;

public class ChronicleApp {

    public static void main(String[] args) {
        // start chronicle
        String queuePath = Paths.get(OS.TMP, "chronicle-test").toString();
        IOTools.deleteDirWithFiles(queuePath);
        SingleChronicleQueue queue = SingleChronicleQueueBuilder
                .single(queuePath)
                .rollCycle(RollCycles.TEN_MINUTELY)
                .build();


        Histogram histogram1 = new Histogram(3);
        Histogram histogram2 = new Histogram(3);
        Histogram histogram3 = new Histogram(3);

        // chronicle reader
        Thread readerThread = new Thread(() -> {
            MethodReader methodReader = queue.createTailer().methodReader(new DummyEventService() {
                @Override
                public void onEvent(DummyEvent event) {
                    long oneHopDelay = System.nanoTime() - event.getCreatedTime();
                    histogram1.recordValue(oneHopDelay);
                    histogram2.recordValue(event.getEnqueueDelay());
                    histogram3.recordValue(oneHopDelay + event.getEnqueueDelay());
                }
            });

            while (true) {
                methodReader.readOne();
            }
        });

        startHistoReader(histogram1, histogram2, histogram3);

        // start reader
        readerThread.start();

        final AtomicLong startTime = new AtomicLong(System.nanoTime());
        DummyEvent dummyEvent = new DummyEvent();
        DummyEventService service = queue.acquireAppender().methodWriter(DummyEventService.class);
        while (true) {
            // publish to queue
            long timeNow = System.nanoTime();
            dummyEvent.setCreatedTime(timeNow);
            dummyEvent.setEnqueueDelay(timeNow - startTime.get());
            startTime.set(timeNow);

            service.onEvent(dummyEvent);
        }
    }

}
