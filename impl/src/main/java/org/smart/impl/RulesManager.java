package org.smart.impl;

import javax.jms.*;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;

/**
 * High-fidelity, schema-agnostic RulesManager for SMART.
 * Consumes AMQP messages and triggers MD-SAL DOMDataTree transactions.
 * No generated YANG classes required.
 */
public class RulesManager implements MessageListener {

    private final DOMDataBroker domDataBroker;
    private final String brokerUrl = "amqp://localhost:5672";
    
    // QNames for SMART YANG Model
    private static final String NAMESPACE = "urn:opendaylight:params:xml:ns:yang:smart";
    private static final String REVISION = "2017-02-15";
    private static final QName SMART_CONTEXT = QName.create(NAMESPACE, REVISION, "smart-context");
    private static final QName FLOWS = QName.create(NAMESPACE, REVISION, "flows");
    private static final QName ID = QName.create(NAMESPACE, REVISION, "id");
    private static final QName STATUS = QName.create(NAMESPACE, REVISION, "status");
    private static final QName IS_CLONED = QName.create(NAMESPACE, REVISION, "is-cloned");

    private Connection connection;

    public RulesManager(DOMDataBroker domDataBroker) {
        this.domDataBroker = domDataBroker;
        initJms();
    }

    private void initJms() {
        try {
            JmsConnectionFactory factory = new JmsConnectionFactory(brokerUrl);
            connection = factory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Destination destination = session.createTopic("smart/flow-updates");
            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(this);
            
            connection.start();
            System.out.println("[SMART-RULES-DOM] Connected to AMQP Broker and listening on smart/flow-updates");
        } catch (Exception e) {
            System.err.println("[SMART-RULES-DOM] JMS Subscribing failed: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String payload = ((TextMessage) message).getText();
                System.out.println("[SMART-RULES-DOM] Received policy update: " + payload);
                
                String flowId = extractField(payload, "FLOW_ID:");
                String decision = extractField(payload, "DECISION:");
                
                if (flowId != null && decision != null) {
                    enforceDecision(flowId, decision);
                }
            }
        } catch (Exception e) {
            System.err.println("[SMART-RULES-DOM] Error processing onMessage: " + e.getMessage());
        }
    }

    /**
     * Enforces the SMART decision using DOMDataBroker.
     * This is schema-agnostic and does not require generated Java bindings.
     */
    private void enforceDecision(String flowId, String decision) {
        if (domDataBroker == null) {
            System.err.println("[SMART-RULES-DOM] DOMDataBroker not available.");
            return;
        }

        // 1. Build the YangInstanceIdentifier for the specific flow
        YangInstanceIdentifier path = YangInstanceIdentifier.builder()
            .node(SMART_CONTEXT)
            .node(FLOWS)
            .nodeWithKey(FLOWS, ID, flowId)
            .node(STATUS)
            .build();

        // 2. Build the NormalizedNode (Status container)
        NormalizedNode<?, ?> statusNode = Builders.containerBuilder()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(STATUS))
            .withChild(Builders.leafBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(IS_CLONED))
                .withValue(decision.equals("CLONE"))
                .build())
            .build();

        // 3. Execute DOM Transaction
        DOMDataWriteTransaction tx = domDataBroker.newWriteOnlyTransaction();
        tx.merge(LogicalDatastoreType.OPERATIONAL, path, statusNode);
        
        try {
            tx.submit().get(); // Synchronous wait for demonstration
            System.out.println("[SMART-ENHANCER-DOM] Successfully merged " + decision + " to MD-SAL for " + flowId);
        } catch (Exception e) {
            System.err.println("[SMART-ENHANCER-DOM] Transaction Failed: " + e.getMessage());
            tx.cancel();
        }
    }

    private String extractField(String payload, String prefix) {
        try {
            return payload.split(prefix)[1].split("\\|")[0];
        } catch (Exception e) { return null; }
    }

    public void close() {
        try { if (connection != null) connection.close(); } catch (Exception e) {}
    }
}
