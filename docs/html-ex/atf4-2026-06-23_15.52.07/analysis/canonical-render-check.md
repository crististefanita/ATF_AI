# Canonical Render Check

rendered_url: http://127.0.0.1:8771/analysis/langgraph-business-understanding.html
screenshot: analysis/canonical-render-check.png
checked_at: 2026-06-23T16:56:00+03:00

Cold render assertions:

- title = Registered Invoice Details
- score_cards = 6
- default_open_details = 1
- graph_panel = 1
- svg_diag = 1
- owned_nodes = 6
- cap_nodes = 1
- Business Flow Graph visible/default-open = true
- Next Closure Pressure visible = true
- Node rule present in legend = true
- MCP Gate References section present = true
- MCP PASS text present in DOM textContent = true
- local MCP links present = 6
- broken_images = 0

Note: collapsed details text is not visible through browser innerText until opened; DOM textContent confirms the final MCP PASS row and links are present. The current graph follows the application-map plus five-run package-overlay pattern.
