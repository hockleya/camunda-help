package org.camunda.bpm.subdivisionincidents;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.runtime.Incident;

import java.util.List;
import java.util.logging.Logger;

/**
 * A custom handler for any incidents created by the subdivision processes or Camunda itself.
 * This is largely guided by this git:
 * https://github.com/camunda/camunda-bpm-platform/blob/master/engine/src/main/java/org/camunda/bpm/engine/impl/incident/DefaultIncidentHandler.java
 */
public class SubdivisionIncidentHandler implements IncidentHandler{

    private final static Logger LOGGER = Logger.getLogger("SUBDIVI-NCIDENT-HANDLER");
    private String type;    //The type of incident this is
    private SubdivisionIncident subdivIncident;

    @Override
    public String getIncidentHandlerType() {
        LOGGER.info(" --- In getIncidentHanderType where type is " + type);
        return type;
    }

    /**
     * Deal with the incident that has been created elsewhere. This handler will simply create an incident to resolve or delete manually.
     * @param incidentContext
     * @param s
     * @return
     */
    @Override
    public Incident handleIncident(IncidentContext incidentContext, String s) {
        LOGGER.info(" --- In handleIncident where message is " + s);
        return subdivIncident.createIncidentFromContext(type, incidentContext, s);
    }

    /**
     * The incident has been resolved and can be deleted.
     * @param incidentContext
     */
    @Override
    public void resolveIncident(IncidentContext incidentContext) {
        LOGGER.info(" --- In resolveIncident");
        this.removeIncident(incidentContext, true);
    }

    /**
     * The incident can be removed, but it was not resolved.
     * @param incidentContext
     */
    @Override
    public void deleteIncident(IncidentContext incidentContext) {
        LOGGER.info(" --- In deleteIncident");
        this.removeIncident(incidentContext, false);
    }

    /**
     * Takes the incident off the incident list. Will check if the incident has been resolved or not.
     * @param incidentContext
     * @param resolved Whether or not the issue has actually been resolved.
     */
    private void removeIncident(IncidentContext incidentContext, boolean resolved) {
        LOGGER.info(" --- In removeIncident where resolved is " + resolved);
        List<Incident> incidents = Context.getCommandContext().getIncidentManager().
                                    findIncidentByConfiguration(incidentContext.getConfiguration());

        for (Incident cI : incidents) {
            IncidentEntity incident = (IncidentEntity) cI;  //Get the incident as a new object

            if (resolved == true)   //Whether or not we resolved the issue decides what we do with it.
                incident.resolve();
            else
                incident.delete();
        }
    }
}
