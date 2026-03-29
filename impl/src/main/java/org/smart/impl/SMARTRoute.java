package org.smart.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core SMART Routing Algorithm.
 * Implements Algorithm 1: SMART Enhancement.
 */
public class SMARTRoute {

    private final BreakpointManager breakpointManager;
    private final Map<String, Boolean> globalCongestionState = new HashMap<>();

    public SMARTRoute(BreakpointManager breakpointManager) {
        this.breakpointManager = breakpointManager;
    }

    /**
     * Executes the SMART routing for a given flow.
     * 
     * @param flowId Unique identifier for the flow.
     * @param origin Source node.
     * @param dest Destination node.
     * @param policies SLA policies.
     * @param currentStatus Flow status metadata (elapsed time, etc.).
     * @param links Current route links.
     * @return Routing decision (Original, Clone, Divert, or Replicate).
     */
    public String execute(String flowId, String origin, String dest, Map<String, Long> policies, Map<String, Object> currentStatus, List<String> links) {
        
        // Check if we are in an adaptive replicate state for this path
        String pathKey = origin + "->" + dest;
        if (globalCongestionState.getOrDefault(pathKey, false)) {
            return "REPLICATE"; // Adaptive replicate for subsequent flows
        }

        // Identify breakpoint if soft threshold met
        String breakpointNode = breakpointManager.markBreakPoint(flowId, policies, currentStatus, links);
        
        if (breakpointNode != null) {
            // Trigger enhancement
            boolean isElephantFlow = (Boolean) currentStatus.getOrDefault("isElephant", false);
            
            if (isElephantFlow) {
                globalCongestionState.put(pathKey, true); // Mark path as congested for others
                return "CLONE"; // Clone subflow for elephant flows
            } else {
                return "DIVERT"; // Divert subflow for smaller priority flows
            }
        }

        return "BASE_ROUTING"; // Stick to shortest path/ECMP
    }

    public void resetCongestion(String path) {
        globalCongestionState.put(path, false);
    }
}
