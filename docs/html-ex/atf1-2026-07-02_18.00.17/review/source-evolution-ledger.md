# Source Evolution Ledger

baseline_before_run_0: previous ATF1 support-intake package capped at 92
final_before_closeout: customer-visible support case after admin reply
baseline git status: dirty worktree with generated ATF1 UI support/test package changes
final git status: dirty worktree frozen into this package root, protected roots excluded from root diff
score_delta: +3
cap: 95
blocker: none expected after focused root diff and live UI proof
repair: rerun MCP package close-out if KPI matrix disagrees
mcp-kpi-matrix: [validation/mcp/mcp-kpi-matrix.md](../validation/mcp/mcp-kpi-matrix.md)
root diff: [review/root-source-thin-no-index.diff](root-source-thin-no-index.diff)

root_diff_files_count: 6
src_main_files: 3
src_test_files: 3
files_added: 6
files_modified: 0
files_deleted: 0
test_methods_added: 5
protected_root_dirty_count: 0
run_01_files_count: 2
run_02_files_count: 2
run_03_files_count: 2
run_04_files_count: 2
run_05_files_count: 2

business novelty ratio: 100
diff efficiency: 117
test value density: 140
support reuse ratio: 100
duplication burn-down: duplicate_action_line_ratio below threshold, duplicate_assertion_line_ratio below threshold after focused frontier diff
protected-root purity: 100
artifact truth score: 100
run learning yield: 100

## Run Delta Rows

| Run | Delta | Diff | State | Source path |
|---|---|---|---|---|
| run_01_delta | Flow plus customer-visible lifecycle tests | [diff](../runs/run_01/review/thin-no-index.diff) | [state](../runs/run_01/review/run-state.json) | `src/main/java/com/endava/ai/ui/support/flows/support/RefundSupportRequestFlow.java` |
| run_02_delta | Admin/customer messages API client plus lifecycle tests | [diff](../runs/run_02/review/thin-no-index.diff) | [state](../runs/run_02/review/run-state.json) | `src/main/java/com/endava/ai/ui/support/service/SupportMessagesClient.java` |
| run_03_delta | Progressed support context plus lifecycle tests | [diff](../runs/run_03/review/thin-no-index.diff) | [state](../runs/run_03/review/run-state.json) | `src/main/java/com/endava/ai/ui/support/model/RefundSupportRequestContext.java` |
| run_04_delta | Shared request cache plus lifecycle tests | [diff](../runs/run_04/review/thin-no-index.diff) | [state](../runs/run_04/review/run-state.json) | `src/test/java/com/endava/ai/ui/tests/refunds/support/RefundSupportRequestTestSupport.java` |
| run_05_delta | Suite routing plus lifecycle tests | [diff](../runs/run_05/review/thin-no-index.diff) | [state](../runs/run_05/review/run-state.json) | `src/test/resources/ui/testng-atf1-refund-support-customer-messages-run.xml` |

## Business Claims Owned

- owned business claim: customer refund support request creates a durable message id.
- owned business claim: the support case starts as NEW and is tied to invoice, amount and customer.
- owned business claim: admin reply creates a durable reply id and references the invoice.
- owned business claim: customer-visible case status becomes IN_PROGRESS after admin reply.
- owned business claim: customer messages API and account page reopen the progressed case.
- owned business claim: invoice, subject and status remain in one customer-visible proof chain.
- owned business claim: the next unowned object is only the refund decision history record, not this support-case lifecycle.


