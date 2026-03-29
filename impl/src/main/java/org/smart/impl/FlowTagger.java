package org.smart.impl;

import javax.jms.*;
import org.apache.qpid.jms.JmsConnectionFactory;

/**
 * Real implementation of FlowTagger using Messaging4Transport patterns.
 * Publishes flow enhancement policies to an AMQP broker.
 */
public class FlowTagger {

    private final SMARTRoute smartRoute;
    private final String brokerUrl = "amqp://localhost:5672";
    
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public FlowTagger(SMARTRoute smartRoute) {
        this.smartRoute = smartRoute;
        initJms();
    }

    /**
     * Initializes the JMS connection to the AMQP broker.
     */
    private void initJms() {
        try {
            JmsConnectionFactory factory = new JmsConnectionFactory(brokerUrl);
            connection = factory.createConnection();
            connection.start();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic("smart/flow-updates");
            producer = session.createProducer(destination);
            
            System.out.println("[SMART-TAGGER-REAL] Connected to AMQP Broker at " + brokerUrl);
        } catch (Exception e) {
            System.err.println("[SMART-TAGGER-REAL] JMS Initialization failed: " + e.getMessage());
        }
    }

    /**
     * Tags a flow and publishes the update to the AMQP topic.
     * 
     * @param flowId Unique identifier for the flow.
     * @param decision Routing decision (CLONE, DIVERT, etc.).
     * @return The generated tag.
     */
    public String tagFlow(String flowId, String decision) {
        String tag = "SMART_VER_1.0|FLOW_ID:" + flowId + "|DECISION:" + decision;
        
        // Publish to AMQP Topic
        publishPolicyUpdate("smart/flow-updates", tag);
        
        return tag;
    }

    /**
     * Publishes a message to a specific topic.
     */
    public void publishPolicyUpdate(String topic, String payload) {
        try {
            if (session == null || producer == null) {
                System.err.println("[SMART-TAGGER-REAL] JMS Session not initialized. Skipping publish.");
                return;
            }
            TextMessage message = session.createTextMessage(payload);
            producer.send(message);
            System.out.println("[SMART-TAGGER-REAL] Published enhancement to " + topic + ": " + payload);
        } catch (JMSException e) {
            System.err.println("[SMART-TAGGER-REAL] Failed to publish message: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (JMSException e) {
            // Log silent
        }
    }
}
