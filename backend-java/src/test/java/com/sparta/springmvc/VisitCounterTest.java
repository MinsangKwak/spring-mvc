package com.sparta.springmvc;

import com.sparta.springmvc.html.HtmlController;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import static org.assertj.core.api.Assertions.assertThat;

class VisitCounterTest {
    @Test
    void concurrentVisitsDoNotLoseIncrements() throws Exception {
        HtmlController controller = new HtmlController();
        Set<Long> values = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            var tasks = IntStream.range(0, 200).<Callable<Void>>mapToObj(i -> () -> {
                var model = new ExtendedModelMap();
                controller.dynamic(model);
                values.add((Long) model.get("visits"));
                return null;
            }).toList();
            for (var task : pool.invokeAll(tasks)) task.get();
            assertThat(values).hasSize(200).contains(1L, 200L);
        } finally {
            pool.shutdownNow();
        }
    }
}
