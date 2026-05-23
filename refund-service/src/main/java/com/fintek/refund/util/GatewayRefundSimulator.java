package com.fintek.refund.util;

import com.fintek.refund.entity.RefundRecord;
import com.fintek.refund.exception.RefundException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;
import org.springframework.stereotype.Component;

@Component
public class GatewayRefundSimulator {
    public String refund(RefundRecord refund) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future = executor.submit(() -> {
                if (refund.getReason().toLowerCase().contains("simulate gateway failure")) {
                    throw new RefundException(502, "Gateway refund simulation failed");
                }
                Thread.sleep(20);
                return "gwr_" + UUID.randomUUID().toString().replace("-", "");
            });
            return future.get(Duration.ofSeconds(2).toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new RefundException(503, "Refund gateway call interrupted");
        } catch (ExecutionException | TimeoutException error) {
            throw new RefundException(502, "Refund gateway call failed");
        }
    }
}
