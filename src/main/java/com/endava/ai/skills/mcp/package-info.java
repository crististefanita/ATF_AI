/**
 * MCP package validation framework.
 *
 * <p>The public flow is deliberately small:</p>
 * <ol>
 *     <li>{@link com.endava.ai.skills.mcp.McpGeneratedPackageValidator} chooses a
 *     profile and executes a list of validation strategies.</li>
 *     <li>{@link com.endava.ai.skills.mcp.rules.McpValidationRule} implementations
 *     inspect package evidence and add violations to
 *     {@link com.endava.ai.skills.mcp.McpPackageValidationResult}.</li>
 *     <li>{@link com.endava.ai.skills.mcp.contract.McpContract} and
 *     {@link com.endava.ai.skills.mcp.contract.checks.McpCheckRef} keep rule and test
 *     intent visible in diagnostics, reports, and negative scenarios.</li>
 * </ol>
 *
 * <p>Core patterns used here: Strategy for validation rules, Facade/Profile
 * selection in {@code McpDefaultRules}, Registry for executable rule families,
 * Builder for rich violations, and immutable value objects for context and
 * contract metadata.</p>
 */
package com.endava.ai.skills.mcp;
