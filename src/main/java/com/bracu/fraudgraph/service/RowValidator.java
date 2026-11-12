package com.bracu.fraudgraph.service;

import com.bracu.fraudgraph.domain.ImportIssue;

import java.util.List;
import java.util.Map;

/** Checks one CSV row. Returns an empty list when the row is fine. */
public interface RowValidator {

    List<ImportIssue> validate(int rowNumber, Map<String, String> row);
}
