# Sprint 1 - Humayra Adiba (23201401) - F13 Rapid Onward Transfer Detection

## Merge into team repo (do this, in order)
1. Copy into the repo's existing package (change `com.bracu.fraudgraph` if the team uses another):
   - detector/PatternDetector.java (skip if it already exists), detector/ForwardingDetector.java
   - service/ForwardingAnalysisService.java, controller/AnalysisController.java
   - templates/forwarding.html, test/.../ForwardingDetectorTest.java
2. Domain stubs (domain/Transaction, GraphSnapshot, DetectionRule, PatternMatch): ONLY copy a stub if the team class does not exist yet. If theirs exists, delete my stub and adapt the getters used by ForwardingDetector:
   Transaction: getTransactionId, getSenderAccountKey, getReceiverAccountKey, getAmount, getOccurredAt, isCompleted
   GraphSnapshot: getCompletedTransactions
   DetectionRule: getRuleId, getVersion, getParameters, isApplicable
   PatternMatch constructor + getFingerprint
3. Do NOT copy my pom.xml or FraudGraphApplication.java over the team's.
4. Branch: git checkout -b feature/f13-rapid-onward-transfer ; commit ; push ; open PR.

## Behaviour (R13)
- Match when 0 <= (outgoing time - incoming time) <= maxGapMinutes (default 5, inclusive), same account, both Completed.
- 3 min matches a 5-min rule; 7 min does not. Outgoing-before-incoming excluded.
- Explanation states a match does not prove identical funds or fraud.

## SRS Section 7 (Sprint 1 row)
Goals Set: Implement F13 rapid onward transfer detection (ForwardingDetector) with configurable time limit, explanation text and tests; contribute to project setup.
Attainment: <fill on 12 Nov: Done only if merged and tested in the team repo>
Features Completed: F13 (only if Done)

## SRS Section 8 (Testing, F13)
JUnit tests in ForwardingDetectorTest: 3 min matches 5-min rule; 7 min does not; exactly 5 min matches; 5 min 1 s does not; reversed timestamps excluded; non-Completed ignored; different accounts; disabled rule; default threshold; invalid threshold (0, -3, abc) rejected; repeat run gives identical fingerprints.

## SRS Section 9 (AI disclosure) - be honest, fill your own Yes/No
F13 ForwardingDetector, tests, controller/view | Claude | Generated first draft of code and tests from SRS | only answer Yes if you can explain every line
