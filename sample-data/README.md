# Sample data (synthetic, for demonstration only)

| File | What should happen |
|---|---|
| `valid_10_rows.csv` | **F1:** 10 rows saved once, in one new snapshot. Upload it a second time: "already imported", nothing new saved. |
| `with_errors.csv` | **F2:** T101, T105, T106, T107 saved. Rejected: row 3 missing sender, row 4 negative amount, row 5 bad timestamp, row 9 conflicting ID T106. Row 7 is an exact repeat of T105, so it is ignored. |
| `bad_header.csv` | Whole file rejected because the header is missing required columns. |
