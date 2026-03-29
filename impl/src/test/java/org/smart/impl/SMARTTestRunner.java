package org.smart.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone Test Runner for SMART logic.
 */
public class SMARTTestRunner {

    public static void main(String[] args) {
        BreakpointManager breakpointManager = new BreakpointManager();
        SMARTRoute smartRoute = new SMARTRoute(breakpointManager);

        System.out.println("--- SMART Core Logic Verification ---");

        // Test 1: Base Routing
        testBaseRouting(smartRoute);

        // Test 2: Divert (Soft Threshold)
        testDivert(smartRoute);

        // Test 3: Clone (Elephant Flow)
        testClone(smartRoute);

        // Test 4: Adaptive Replicate
        testAdaptiveReplicate(smartRoute);

        System.out.println("--- Verification Complete ---");
    }

    private static void testBaseRouting(SMARTRoute smartRoute) {
        Map<String, Long> policies = new HashMap<>();
        policies.put("threshold-ms", 1000L);
        policies.put("soft-threshold-ms", 800L);

        Map<String, Object> status = new HashMap<>();
        status.put("elapsedTime", 500L);
        status.put("origin", "host1");

        List<String> links = Arrays.asList("sw1", "sw2", "sw3");

        String decision = smartRoute.execute("flow1", "host1", "host2", policies, status, links);
        assertEquals("BASE_ROUTING", decision, "Test 1: Base Routing");
    }

    private static void testDivert(SMARTRoute smartRoute) {
        Map<String, Long> policies = new HashMap<>();
        policies.put("threshold-ms", 1000L);
        policies.put("soft-threshold-ms", 800L);

        Map<String, Object> status = new HashMap<>();
        status.put("elapsedTime", 850L);
        status.put("origin", "host1");
        status.put("isElephant", false);

        List<String> links = Arrays.asList("sw1", "sw2", "sw3");

        String decision = smartRoute.execute("flow2", "host1", "host2", policies, status, links);
        assertEquals("DIVERT", decision, "Test 2: Divert");
    }

    private static void testClone(SMARTRoute smartRoute) {
        Map<String, Long> policies = new HashMap<>();
        policies.put("threshold-ms", 1000L);
        policies.put("soft-threshold-ms", 800L);

        Map<String, Object> status = new HashMap<>();
        status.put("elapsedTime", 900L);
        status.put("origin", "host1");
        status.put("isElephant", true);

        List<String> links = Arrays.asList("sw1", "sw2", "sw3");

        String decision = smartRoute.execute("flow-ele", "host1", "host2", policies, status, links);
        assertEquals("CLONE", decision, "Test 3: Clone");
    }

    private static void testAdaptiveReplicate(SMARTRoute smartRoute) {
        Map<String, Long> policies = new HashMap<>();
        policies.put("threshold-ms", 1000L);
        policies.put("soft-threshold-ms", 800L);

        Map<String, Object> status = new HashMap<>();
        status.put("elapsedTime", 100L); 
        status.put("origin", "host1");

        List<String> links = Arrays.asList("sw1", "sw2", "sw3");

        // The previous test (Test 3) set global congestion for host1->host2
        String decision = smartRoute.execute("flow-next", "host1", "host2", policies, status, links);
        assertEquals("REPLICATE", decision, "Test 4: Adaptive Replicate");
    }

    private static void assertEquals(String expected, String actual, String testName) {
        if (expected.equals(actual)) {
            System.out.println("[PASS] " + testName + " - Got: " + actual);
        } else {
            System.err.println("[FAIL] " + testName + " - Expected: " + expected + ", Got: " + actual);
        }
    }
}
