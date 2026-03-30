# SMART: SDN Middlebox Architecture for Resilient Transfers

SMART is a research framework for achieving differentiated Quality of Service (QoS) in multi-tenant clouds using selective redundancy.

## Key Features
- **Selective Redundancy Algorithms**: Adaptive logic for `CLONE`, `DIVERT`, and `REPLICATE` actions based on SLA soft-thresholds.
- **Dynamic Breakpoint Discovery**: Real-time identification of congestion points using ODL Operational Port Statistics.
- **Schema-Agnostic AMQP Propagation**: Integration with **Messaging4Transport** using **Apache Qpid JMS** and **DOMDataBroker** for high-performance, schema-agnostic flow management.
- **SENDIM-Ready Verification**: Seamless verification via unit tests and SENDIM-style Python emulation.

## Project Structure
- `api/`: contains the YANG models (`smart.yang`) for flow data and policy definitions.
- `impl/`: The core implementation of SMART algorithms and ODL/AMQP integration.
- `scripts/`: SENDIM-style Python scripts for Mininet emulation and logic validation.

## Build & Installation
### Prerequisites
- Java 8 (for ODL Beryllium compatibility)
- Maven 3.8.7+
- Docker & Docker Compose

### Build the Project
```bash
mvn clean install -DskipTests
```

## Verification
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

## Deployment with Docker
To simplify the setup of the AMQP broker and other dependencies, a `docker-compose.yml` is provided.

### Start the AMQP Broker
The project requires an AMQP 1.0 compatible broker (e.g., Apache ActiveMQ).
```bash
docker-compose up -d
```
The ActiveMQ web console is available at `http://localhost:8161` (default credentials: `admin`/`admin`).

## OpenDaylight & Messaging4Transport Integration
SMART is designed to run as an OSGi bundle within the OpenDaylight (ODL) Beryllium environment.

### Architecture Overview
- **Messaging4Transport (M4T)**: Used as the transport layer for policy propagation. SMART components publish/subscribe to the `smart/flow-updates` AMQP topic.
- **SDN Actuation**: The `RulesManager` interacts with the ODL `DOMDataBroker` to perform schema-agnostic updates to the Operational Data Store.
- **Congestion Detection**: The `BreakpointManager` queries the ODL Inventory Operational Data for port statistics (`bytes-transmitted`, `packets-received`) to identify link saturation.

### Configuration
The default broker URL is `amqp://localhost:5672`. This can be updated in `SmartProvider` or passed via ODL Configuration Subsystem.

## Citing SMART
If you use SMART in your research, please cite the following papers:

* Kathiravelu, P. and Veiga, L., 2016, October. **Selective Redundancy in Network-as-a-Service: Differentiated QoS in Multi-tenant Clouds.** In OTM Confederated International Conferences "On the Move to Meaningful Internet Systems" (pp. 87-97). Cham: Springer International Publishing.

* Kathiravelu, P. and Veiga, L., 2017, May. **SDN middlebox architecture for resilient transfers.** In 2017 IFIP/IEEE Symposium on Integrated Network and Service Management (IM) (pp. 560-563). IEEE.
