package com.bracu.fraudgraph.detector;

import com.bracu.fraudgraph.domain.DetectionRule;
import com.bracu.fraudgraph.domain.GraphSnapshot;
import com.bracu.fraudgraph.domain.PatternMatch;
import com.bracu.fraudgraph.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ForwardingDetectorTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 10, 10, 0, 0);

    private static Transaction tx(String id, String from, String to, LocalDateTime at, String status) {
        return new Transaction(id, from, to, BigDecimal.valueOf(1000), at, status);
    }

    private static DetectionRule rule(String maxGap, boolean enabled) {
        Map<String, String> p = maxGap == null ? Map.of() : Map.of("maxGapMinutes", maxGap);
        return new DetectionRule(13L, 1, "RAPID_ONWARD_TRANSFER", p, enabled);
    }

    private static GraphSnapshot snap(Transaction... ts) {
        return new GraphSnapshot(1L, T0, List.of(ts));
    }

    private static List<PatternMatch> run(DetectionRule r, Transaction... ts) {
        return new ForwardingDetector(r).detect(snap(ts));
    }

    @Test
    void threeMinutesMatchesFiveMinuteRule() {
        var m = run(rule("5", true),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(3), "Completed"));
        assertEquals(1, m.size());
        assertEquals(List.of("T1", "T2"), m.get(0).getOrderedTransactionIds());
        assertEquals("RAPID_ONWARD_TRANSFER", m.get(0).getPatternType());
        assertEquals(1, m.get(0).getRuleVersion());
    }

    @Test
    void sevenMinutesDoesNotMatchFiveMinuteRule() {
        assertTrue(run(rule("5", true),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(7), "Completed")).isEmpty());
    }

    @Test
    void exactlyAtLimitMatchesAndOneSecondOverDoesNot() {
        assertEquals(1, run(rule("5", true),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(5), "Completed")).size());
        assertTrue(run(rule("5", true),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(5).plusSeconds(1), "Completed")).isEmpty());
    }

    @Test
    void outgoingBeforeIncomingIsExcluded() {
        assertTrue(run(rule("5", true),
                tx("T1", "A", "B", T0.plusMinutes(3), "Completed"),
                tx("T2", "B", "C", T0, "Completed")).isEmpty());
    }

    @Test
    void nonCompletedTransactionsAreIgnored() {
        assertTrue(run(rule("5", true),
                tx("T1", "A", "B", T0, "Failed"),
                tx("T2", "B", "C", T0.plusMinutes(1), "Completed")).isEmpty());
        assertTrue(run(rule("5", true),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(1), "Pending")).isEmpty());
    }

    @Test
    void differentAccountsDoNotMatch() {
        assertTrue(run(rule("5", true),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "X", "C", T0.plusMinutes(1), "Completed")).isEmpty());
    }

    @Test
    void disabledRuleProducesNothing() {
        assertTrue(run(rule("5", false),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(1), "Completed")).isEmpty());
    }

    @Test
    void defaultsToFiveMinutesWhenParameterMissing() {
        assertEquals(1, run(rule(null, true),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(4), "Completed")).size());
    }

    @Test
    void invalidThresholdIsRejected() {
        var snapshot = snap(tx("T1", "A", "B", T0, "Completed"));
        assertThrows(IllegalArgumentException.class, () -> new ForwardingDetector(rule("0", true)).detect(snapshot));
        assertThrows(IllegalArgumentException.class, () -> new ForwardingDetector(rule("-3", true)).detect(snapshot));
        assertThrows(IllegalArgumentException.class, () -> new ForwardingDetector(rule("abc", true)).detect(snapshot));
    }

    @Test
    void sameSnapshotTwiceGivesIdenticalFingerprints() {
        Transaction[] ts = {
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(2), "Completed"),
                tx("T3", "B", "D", T0.plusMinutes(4), "Completed")};
        var first = run(rule("5", true), ts).stream().map(PatternMatch::getFingerprint).toList();
        var second = run(rule("5", true), ts).stream().map(PatternMatch::getFingerprint).toList();
        assertEquals(2, first.size());
        assertEquals(first, second);
    }

    @Test
    void explanationStatesItDoesNotProveSameFunds() {
        var m = run(rule("5", true),
                tx("T1", "A", "B", T0, "Completed"),
                tx("T2", "B", "C", T0.plusMinutes(3), "Completed"));
        assertTrue(m.get(0).getExplanation().contains("does not show the same funds"));
    }
}
