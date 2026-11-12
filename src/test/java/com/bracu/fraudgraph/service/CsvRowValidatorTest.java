package com.bracu.fraudgraph.service;

import com.bracu.fraudgraph.domain.ImportIssue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F2 tests for one row at a time. */
class CsvRowValidatorTest {

    private final CsvRowValidator validator = new CsvRowValidator();

    /** A correct row. Each test breaks one thing. */
    private Map<String, String> goodRow() {
        Map<String, String> row = new HashMap<>();
        row.put("transactionId", "T001");
        row.put("senderProvider", "BKASH");
        row.put("senderAccountId", "A001");
        row.put("receiverProvider", "NAGAD");
        row.put("receiverAccountId", "B001");
        row.put("amount", "1500.00");
        row.put("timestamp", "2026-11-01T10:00:00");
        row.put("status", "Completed");
        return row;
    }

    @Test
    void goodRowHasNoIssues() {
        assertTrue(validator.validate(2, goodRow()).isEmpty());
    }

    @Test
    void missingSenderIsRejected() {
        Map<String, String> row = goodRow();
        row.put("senderAccountId", "");

        List<ImportIssue> issues = validator.validate(2, row);

        assertEquals(1, issues.size());
        assertEquals("Missing senderAccountId", issues.get(0).getReason());
        assertEquals(2, issues.get(0).getRowNumber());
    }

    @Test
    void zeroAmountIsRejected() {
        Map<String, String> row = goodRow();
        row.put("amount", "0");

        assertEquals(1, validator.validate(2, row).size());
    }

    @Test
    void negativeAmountIsRejected() {
        Map<String, String> row = goodRow();
        row.put("amount", "-50");

        assertEquals(1, validator.validate(2, row).size());
    }

    @Test
    void amountThatIsNotANumberIsRejected() {
        Map<String, String> row = goodRow();
        row.put("amount", "abc");

        assertEquals(1, validator.validate(2, row).size());
    }

    @Test
    void badTimestampIsRejected() {
        Map<String, String> row = goodRow();
        row.put("timestamp", "yesterday");

        assertEquals(1, validator.validate(2, row).size());
    }
}
