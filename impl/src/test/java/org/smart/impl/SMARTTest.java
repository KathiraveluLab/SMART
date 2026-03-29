package org.smart.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMARTTest {

    private SMARTRoute smartRoute;
    private BreakpointManager breakpointManager;

    @Before
    public void setUp() {
        breakpointManager = new BreakpointManager();
        smartRoute = new SMARTRoute(breakpointManager);
    }

    @Test
    public void testBaseRoutingWhenNoCongestion() {
        Map<String, Long> policies = new HashMap<>();
        policies.put("threshold-ms", 1000L);
        policies.put("soft-threshold-ms", 800L);

        Map<String, Object> status = new HashMap<>();
        status.put("elapsedTime", 500L);
        status.put("origin", "host1");

        List<String> links = Arrays.asList("sw1", "sw2", "sw3");

        String decision = smartRoute.execute("flow1", "host1", "host2", policies, status, links);
        assertEquals("BASE_ROUTING", decision);
    }

    @Test
    public void testDivertWhenSoftThresholdMet() {
        Map<String, Long> policies = new HashMap<>();
        policies.put("threshold-ms", 1000L);
        policies.put("soft-threshold-ms", 800L);

        Map<String, Object> status = new HashMap<>();
        status.put("elapsedTime", 850L);
        status.put("origin", "host1");
        status.put("isElephant", false);

        List<String> links = Arrays.asList("sw1", "sw2", "sw3");

        String decision = smartRoute.execute("flow1", "host1", "host2", policies, status, links);
        assertEquals("DIVERT", decision);
    }

    @Test
    public void testCloneWhenElephantFlow() {
        Map<String, Long> policies = new HashMap<>();
        policies.put("threshold-ms", 1000L);
        policies.put("soft-threshold-ms", 800L);

        Map<String, Object> status = new HashMap<>();
        status.put("elapsedTime", 900L);
        status.put("origin", "host1");
        status.put("isElephant", true);

        List<String> links = Arrays.asList("sw1", "sw2", "sw3");

        String decision = smartRoute.execute("flow-ele", "host1", "host2", policies, status, links);
        assertEquals("CLONE", decision);
    }

    @Test
    public void testAdaptiveReplicate() {
        Map<String, Long> policies = new HashMap<>();
        policies.put("threshold-ms", 1000L);
        policies.put("soft-threshold-ms", 800L);

        Map<String, Object> status = new HashMap<>();
        status.put("elapsedTime", 900L);
        status.put("origin", "host1");
        status.put("isElephant", true);

        List<String> links = Arrays.asList("sw1", "sw2", "sw3");

        // Trigger CLONE and set global state
        smartRoute.execute("flow-prev", "host1", "host2", policies, status, links);
        
        // Subsequent flow on same path
        Map<String, Object> nextStatus = new HashMap<>();
        nextStatus.put("elapsedTime", 100L); // High performance flow
        
        String decision = smartRoute.execute("flow-next", "host1", "host2", policies, nextStatus, links);
        assertEquals("REPLICATE", decision);
    }
}
