package com.bracu.fraudgraph.service;

import com.bracu.fraudgraph.domain.ImportIssue;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * F2: checks a single row of the CSV file.
 *
 *  1. every required field is filled in
 *  2. amount is a number greater than 0
 *  3. timestamp is a real date-time, e.g. 2026-11-01T10:00:00 (Bangladesh time)
 *
 * Duplicate IDs are checked in ImportService, because that needs to see the other rows.
 */
@Component
public class CsvRowValidator implements RowValidator {

    public static final List<String> REQUIRED_COLUMNS = List.of(
            "transactionId", "senderProvider", "senderAccountId",
            "receiverProvider", "receiverAccountId", "amount", "timestamp", "status");

    @Override
    public List<ImportIssue> validate(int rowNumber, Map<String, String> row) {
        List<ImportIssue> issues = new ArrayList<>();
        String id = row.get("transactionId");

        // 1. Required fields
        for (String column : REQUIRED_COLUMNS) {
            if (isBlank(row.get(column))) {
                issues.add(new ImportIssue(rowNumber, id, "Missing " + column));
            }
        }

        // 2. Amount must be a positive number
        String amount = row.get("amount");
        if (!isBlank(amount)) {
            try {
                if (new BigDecimal(amount).compareTo(BigDecimal.ZERO) <= 0) {
                    issues.add(new ImportIssue(rowNumber, id, "Amount must be greater than 0 (was " + amount + ")"));
                }
            } catch (NumberFormatException e) {
                issues.add(new ImportIssue(rowNumber, id, "Amount is not a number (was " + amount + ")"));
            }
        }

        // 3. Timestamp must be a valid date and time
        String timestamp = row.get("timestamp");
        if (!isBlank(timestamp)) {
            try {
                LocalDateTime.parse(timestamp);
            } catch (DateTimeParseException e) {
                issues.add(new ImportIssue(rowNumber, id,
                        "Timestamp is not valid (was " + timestamp + ", expected like 2026-11-01T10:00:00)"));
            }
        }

        return issues;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
