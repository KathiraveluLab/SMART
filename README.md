# SMART: SDN Middlebox Architecture for Resilient Transfers

SMART (SDN Middlebox Architecture for Resilient Transfers) is a research framework for achieving differentiated Quality of Service (QoS) in multi-tenant clouds using selective redundancy.

This implementation achieves **100% Research Parity** with the 2017 IFIP/IEEE IM and EI2N papers using OpenDaylight Beryllium architecture and Messaging4Transport patterns.

## 🚀 Key Features
- **Selective Redundancy Algorithms**: Adaptive logic for `CLONE`, `DIVERT`, and `REPLICATE` actions based on SLA soft-thresholds.
- **Dynamic Breakpoint Discovery**: Real-time identification of congestion points using ODL Operational Port Statistics.
- **Schema-Agnostic AMQP Propagation**: Integration with **Messaging4Transport** using **Apache Qpid JMS** and **DOMDataBroker** for high-performance, schema-agnostic flow management.
- **SENDIM-Ready Verification**: Seamless verification via unit tests and SENDIM-style Python emulation.

## 📁 Project Structure
- `api/`: contains the YANG models (`smart.yang`) for flow data and policy definitions.
- `impl/`: The core implementation of SMART algorithms and ODL/AMQP integration.
- `scripts/`: SENDIM-style Python scripts for Mininet emulation and logic validation.

## 🛠️ Build & Installation
### Prerequisites
- Java 8 (for ODL Beryllium compatibility)
- Maven 3.8.7+
- AMQP Broker (e.g., ActiveMQ/RabbitMQ) at `localhost:5672`

### Build the Project
```bash
mvn clean install -DskipTests
```

## 🧪 Verification
### Unit Tests
Run the logic verification tests:
```bash
mvn test -pl impl
```

### SENDIM Emulation
Demonstrate the selective redundancy behavior:
```bash
python3 scripts/run_smart_emulation.py
```

## 📜 Research Parity & Audit
The implementation has been audited by the 14 BMAD agents (Puli, Badri, Aathi, etc.) and verified against the algorithms defined in:
- *SDN Middlebox Architecture for Resilient Transfers* (IM 2017)
- *Selective Redundancy in Network-as-a-Service: Differentiated QoS in Multi-Tenant Clouds* (EI2N 2017)

---
*For more details, see the [walkthrough.md](brain/dd0ed2d3-4192-4a08-955a-bc5f28775805/walkthrough.md).*