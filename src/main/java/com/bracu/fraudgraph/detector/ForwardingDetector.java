package com.bracu.fraudgraph.detector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bracu.fraudgraph.domain.DetectionRule;
import com.bracu.fraudgraph.domain.GraphSnapshot;
import com.bracu.fraudgraph.domain.PatternMatch;
import com.bracu.fraudgraph.domain.Transaction;


public class ForwardingDetector extends PatternDetector {

    public static final String PATTERN_TYPE = "RAPID_ONWARD_TRANSFER";
    public static final String PARAM_MAX_GAP_MINUTES = "maxGapMinutes";
    public static final int DEFAULT_MAX_GAP_MINUTES = 5;

    private final DetectionRule rule;

    public ForwardingDetector(DetectionRule rule) {
        if (rule == null) throw new IllegalArgumentException("rule must not be null");
        this.rule = rule;
    }

    public static int readMaxGapMinutes(DetectionRule rule) {
        String raw = rule.getParameters().get(PARAM_MAX_GAP_MINUTES);
        if (raw == null || raw.isBlank()) return DEFAULT_MAX_GAP_MINUTES;
        try {
            int v = Integer.parseInt(raw.trim());
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(PARAM_MAX_GAP_MINUTES + " must be a positive whole number, got: " + raw);
        }
    }

    @Override
    public List<PatternMatch> detect(GraphSnapshot graph) {
        if (graph == null || !rule.isApplicable()
            || !PATTERN_TYPE.equals(rule.getRuleType())) {
        return List.of();
        }

        Duration limit = Duration.ofMinutes(readMaxGapMinutes(rule));
        Comparator<Transaction> byTimeThenId =
                Comparator.comparing(Transaction::getOccurredAt).thenComparing(Transaction::getTransactionId);

        List<Transaction> completed = new ArrayList<>(graph.getCompletedTransactions());
        completed.sort(byTimeThenId);

        Map<String, List<Transaction>> incomingByAccount = new HashMap<>();
        for (Transaction t : completed) {
            incomingByAccount.computeIfAbsent(t.getReceiverAccountKey(), k -> new ArrayList<>()).add(t);
        }

        List<PatternMatch> matches = new ArrayList<>();
        long nextId = 1;
        for (Transaction out : completed) {
            List<Transaction> candidates = incomingByAccount.get(out.getSenderAccountKey());
            if (candidates == null) continue;
            for (Transaction in : candidates) {
                if (in.getTransactionId().equals(out.getTransactionId())) continue;
                Duration gap = Duration.between(in.getOccurredAt(), out.getOccurredAt());
                if (gap.isNegative() || gap.compareTo(limit) > 0) continue;
                matches.add(new PatternMatch(nextId++, PATTERN_TYPE, List.of(in, out),
                        rule.getRuleId(), rule.getVersion(), explain(in, out, gap, limit)));
            }
        }
        return matches;
    }

    private static String explain(Transaction in, Transaction out, Duration gap, Duration limit) {
        return "Account " + out.getSenderAccountKey() + " received " + in.getAmount() + " BDT (" + in.getTransactionId()
                + ") and sent " + out.getAmount() + " BDT (" + out.getTransactionId() + ") "
                + formatGap(gap) + " later; rule limit is " + limit.toMinutes() + " min. "
                + "This is a pattern to review; it does not show the same funds were forwarded or that fraud occurred.";
    }
    private static String formatGap(Duration gap) {
        long s = gap.getSeconds();
        return (s / 60) + " min " + (s % 60) + " s";
    }
}
