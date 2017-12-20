package org.camunda.bpm.subdivisionincidents;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.runtime.Incident;

import java.util.logging.Logger;

/**
 * Represents an incident specifically made by the subdivision processes. Has constructors for building these new incidents.
 * This is largely guided by this git:
 * https://github.com/camunda/camunda-bpm-platform/blob/master/engine/src/main/java/org/camunda/bpm/engine/impl/incident/DefaultIncidentHandler.java
 */
public class SubdivisionIncident {

    private final static Logger LOGGER = Logger.getLogger("SUBDIV-INCIDENT");

    public Incident createIncidentFromContext(String type, IncidentContext incidentContext, String s) {
        IncidentEntity incident = IncidentEntity.createAndInsertIncident(type, incidentContext, s);

        if(incident.getExecutionId() != null)
            incident.createRecursiveIncidents();

        return incident;
    }

    /**
     * Creates a new incident from information sent through a delegate task's information.
     * @param taskExecution The execution of the current delegate task. Use "DelegateTask.getExecution()" to find this.
     * @param type The type of error that is being thrown.
     * @param message A message to explain the error.
     */
    public void createAndSubmitIncident(DelegateExecution taskExecution, String type, String message) {
        LOGGER.warning("CREATING INCIDENT OF TYPE " + type + " WITH MESSAGE " + message);
        IncidentContext incidentContext = new IncidentContext();
        //Set up the context for the issue, so that the issue itself can be created later
        incidentContext.setActivityId(taskExecution.getCurrentActivityId());
        incidentContext.setActivityId(taskExecution.getProcessInstanceId());
        incidentContext.setActivityId(taskExecution.getProcessDefinitionId());

        IncidentEntity incident = IncidentEntity.createAndInsertIncident(type, incidentContext, message);

        if(incident.getExecutionId() != null)
            incident.createRecursiveIncidents();
    }
}
