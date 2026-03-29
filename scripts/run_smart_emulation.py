#!/usr/bin/env python3
"""
SMART Emulation Script (SENDIM-style)
Simulates a leaf-spine topology and demonstrates SMART's selective redundancy.
"""

import time

class MiniNetSim:
    def __init__(self):
        self.hosts = ["h1", "h2", "h3", "h4"]
        self.switches = ["s1", "s2", "s3", "s4"]
        self.congestion = {"s2": False}

    def run_flow(self, flow_id, priority=False, is_elephant=False):
        print(f"\n[SENDIM] Initializing Flow {flow_id} (Priority: {priority}, Elephant: {is_elephant})")
        path = ["s1", "s2", "s4"]
        elapsed = 0
        
        for node in path:
            print(f"  -> At node {node}")
            if self.congestion.get(node, False):
                print(f"  [!] Congestion detected at {node}!")
                elapsed += 500 # Simulated delay
            else:
                elapsed += 100 # Normal latency
            
            # SMART Threshold Check (Logic from SMARTRoute.java)
            if priority and elapsed >= 400:
                print(f"  [SMART] Soft-Threshold Met at {node}! Triggering Enhancement...")
                if is_elephant:
                    print(f"  [SMART] Action: CLONE subflow to alternative path [s1, s3, s4]")
                    return "SUCCESS (CLONED)"
                else:
                    print(f"  [SMART] Action: DIVERT subflow to alternative path [s1, s3, s4]")
                    return "SUCCESS (DIVERTED)"
            
            time.sleep(0.1)

        if elapsed >= 1000:
            return "SLA_VIOLATION (CONGESTED)"
        return "SUCCESS (BASE_ROUTE)"

def main():
    sim = MiniNetSim()
    
    # 1. Base case: No congestion
    print(sim.run_flow("F1", priority=True))
    
    # 2. Congestion case: Priority flow
    sim.congestion["s2"] = True
    print(sim.run_flow("F2", priority=True, is_elephant=True))
    
    # 3. Congestion case: Non-priority flow
    print(sim.run_flow("F3", priority=False))

if __name__ == "__main__":
    main()
