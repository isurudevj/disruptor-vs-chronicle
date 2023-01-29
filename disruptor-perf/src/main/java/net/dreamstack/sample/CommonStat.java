package net.dreamstack.sample;

import org.HdrHistogram.Histogram;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommonStat {

    public static void startHistoReader(Histogram histogram1, Histogram histogram2, Histogram histogram3) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.printf("Total count = %d\n", histogram1.getTotalCount());
            System.out.printf("1 Hop    Delay = 99.99%% = %d us\n", Math.round(histogram1.getValueAtPercentile(99.99) / 1_000.0f));
            System.out.printf("Enqueue  Delay = 99.99%% = %d us\n", Math.round(histogram2.getValueAtPercentile(99.99) / 1_000.0f));
            System.out.printf("Total  Delay = 99.99%% = %d us\n", Math.round(histogram3.getValueAtPercentile(99.99) / 1_000.0f));

            /*histogram1.reset();
            histogram2.reset();
            histogram3.reset();*/
        }, 10, 10, TimeUnit.SECONDS);
    }

}
