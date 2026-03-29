package org.smart.impl;

import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main provider for the SMART service.
 * Handles lifecycle and wiring of core components in ODL.
 */
public class SmartProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SmartProvider.class);

    private final DOMDataBroker domDataBroker;
    private BreakpointManager breakpointManager;
    private SMARTRoute smartRoute;
    private FlowTagger flowTagger;
    private RulesManager rulesManager;

    public SmartProvider(final DOMDataBroker domDataBroker) {
        this.domDataBroker = domDataBroker;
    }

    /**
     * Called when the BluePrint container is initialized.
     */
    public void init() {
        LOG.info("SMART Provider Session Initiated (Full DOM Integration)");

        // 1. Initialize core logic with DOMDataBroker for stats querying
        breakpointManager = new BreakpointManager(domDataBroker);
        smartRoute = new SMARTRoute(breakpointManager);

        // 2. Initialize Messaging4Transport-style components
        flowTagger = new FlowTagger(smartRoute);
        rulesManager = new RulesManager(domDataBroker);

        LOG.info("SMART Core Components (BreakpointManager, Tagger, RulesManager) initialized.");
    }

    @Override
    public void close() throws Exception {
        LOG.info("SMART Provider Closed");
        if (flowTagger != null) flowTagger.close();
        if (rulesManager != null) rulesManager.close();
    }
}
