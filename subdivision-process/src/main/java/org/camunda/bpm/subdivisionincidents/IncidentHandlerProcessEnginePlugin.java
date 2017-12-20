package org.camunda.bpm.subdivisionincidents;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class IncidentHandlerProcessEnginePlugin  extends AbstractProcessEnginePlugin {

    private final static Logger LOGGER = Logger.getLogger("SETUP");

    @Override
    public void preInit(ProcessEngineConfigurationImpl config) {
        LOGGER.info("Setting up pre init");
        List<IncidentHandler> handlers = new ArrayList<IncidentHandler>();
        handlers.add(new SubdivisionIncidentHandler());
        config.setCustomIncidentHandlers(handlers);
    }
}