package com.ai.core.reporting.graph;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Rendered neutral business graph layout. Reporting owns this shape, not the business truth behind it.
 */
@Value
@Accessors(fluent = true)
public class BusinessGraphLayout {
    String html;
    String svg;
    int nodeCount;
    int edgeCount;
}
