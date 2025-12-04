package com.profileMangager.profile.monitor;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ApiStats {

    // Tổng số request đã xử lý từ khi server start
    private final AtomicLong totalRequests = new AtomicLong(0);

    // Số request ĐANG được xử lý (concurrent)
    private final AtomicInteger currentRequests = new AtomicInteger(0);

    // Tổng thời gian xử lý (ms) của tất cả request
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    // Số request đồng thời lớn nhất từng đạt (max concurrent)
    private final AtomicInteger maxConcurrent = new AtomicInteger(0);


    /** Hàm gọi ở ĐẦU mỗi request */
    public long onRequestStart() {
        long start = System.currentTimeMillis();       // thời điểm bắt đầu

        int current = currentRequests.incrementAndGet(); // +1 request đang chạy
        totalRequests.incrementAndGet();                // +1 tổng request

        // Cập nhật maxConcurrent = max(maxConcurrent hiện tại, current mới)
        maxConcurrent.accumulateAndGet(current, Math::max);

        return start; // trả về thời điểm bắt đầu để lát nữa tính duration
    }

    /** Hàm gọi ở CUỐI mỗi request */
    public void onRequestEnd(long startTimeMs) {
        long duration = System.currentTimeMillis() - startTimeMs;

        currentRequests.decrementAndGet();    // -1 request đang chạy
        totalLatencyMs.addAndGet(duration);   // cộng thêm thời gian xử lý
    }

    /** Trả về "snapshot" các số liệu hiện tại để trả JSON cho client */
    public ApiStatsSnapshot snapshot() {
        long total = totalRequests.get();
        long totalLatency = totalLatencyMs.get();

        double avgLatency = 0.0;
        if (total > 0) {
            avgLatency = (double) totalLatency / total;
        }

        return new ApiStatsSnapshot(
                total,
                currentRequests.get(),
                avgLatency,
                maxConcurrent.get()
        );
    }

    /** DTO đơn giản để trả JSON */
    public record ApiStatsSnapshot(
            long totalRequests,
            int currentRequests,
            double avgLatencyMs,
            int maxConcurrent
    ) {}
}
