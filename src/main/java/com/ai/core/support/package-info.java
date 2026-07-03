/**
 * Shared support kernel for reusable ATF primitives consumed by Core, MCP,
 * Atlas and future skills.
 *
 * <p>This package owns low-level contract annotations, YAML contract loading
 * helpers and small HTML primitives. It must stay domain-neutral: no MCP
 * package-gate logic, no Atlas business-understanding rules and no TestNG
 * test helpers belong here.</p>
 */
package com.ai.core.support;
