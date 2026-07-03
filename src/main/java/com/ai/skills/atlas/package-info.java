/**
 * Atlas validation framework.
 *
 * <p>The public flow is intentionally small:</p>
 * <ol>
 *     <li>{@link com.ai.skills.atlas.AtlasGeneratedOutputValidator} loads the
 *     current atlas context and executes selected validation strategies.</li>
 *     <li>{@link com.ai.skills.atlas.AtlasValidationRuleRegistry} owns the
 *     executable base and candidate rule families.</li>
 *     <li>{@link com.ai.skills.atlas.contract.AtlasRuleCatalog} owns the
 *     diagnostic contract metadata used by reports and tests.</li>
 * </ol>
 *
 * <p>Core patterns used here: Strategy for individual rules, Registry for rule
 * composition, Catalog for human-readable rule metadata, and immutable result
 * objects for validation feedback.</p>
 */
package com.ai.skills.atlas;
