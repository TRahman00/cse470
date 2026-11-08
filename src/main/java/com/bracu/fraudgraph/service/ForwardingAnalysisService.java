package com.bracu.fraudgraph.service;

import com.bracu.fraudgraph.detector.ForwardingDetector;
import com.bracu.fraudgraph.domain.DetectionRule;
import com.bracu.fraudgraph.domain.GraphSnapshot;
import com.bracu.fraudgraph.domain.PatternMatch;
import com.bracu.fraudgraph.domain.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Runs F13 on a snapshot. Business logic stays here, not in the controller (SRS 3.2).
 * TODO (after merge): load the real GraphSnapshot and DetectionRule from Tashfin's/Labib's repositories.
 */
@Service
public class ForwardingAnalysisService {

    public List<PatternMatch> run(GraphSnapshot snapshot, DetectionRule rule) {
        return new ForwardingDetector(rule).detect(snapshot);
    }

    public DetectionRule defaultRule(int maxGapMinutes) {
        return new DetectionRule(13L, 1, "RAPID_ONWARD_TRANSFER",
                Map.of(ForwardingDetector.PARAM_MAX_GAP_MINUTES, String.valueOf(maxGapMinutes)), true);
    }

    /** Synthetic demo data (SRS: all data synthetic). */
    public GraphSnapshot demoSnapshot() {
        LocalDateTime t = LocalDateTime.of(2026, 11, 10, 10, 0);
        return new GraphSnapshot(1L, t, List.of(
                tx("T1", "BKASH:A01", "BKASH:B02", 5000, t),
                tx("T2", "BKASH:B02", "NAGAD:C03", 4900, t.plusMinutes(3)),   // 3 min: match
                tx("T3", "NAGAD:C03", "ROCKET:D04", 4800, t.plusMinutes(10)), // 7 min after T2: no match at 5
                tx("T4", "BKASH:A01", "BKASH:E05", 300, t.plusMinutes(1))));
    }

    private static Transaction tx(String id, String from, String to, int amount, LocalDateTime at) {
        return new Transaction(id, from, to, BigDecimal.valueOf(amount), at, "Completed");
    }
}
