package org.camunda.bpm.subdivisionprocess;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.logging.Logger;

public class IdentifyPropertiesAssignmentListener implements TaskListener {
	
	private static final Logger LOGGER = Logger.getLogger("LOAN-ASSIGNMENTS");
	
	/**
	* Initiate the task assignment listener.
	* @param delTask The delegated task, this contains info about the job and the user it was assigned to.
	*/
	public void notify(DelegateTask delTask) {

		String assignedUser = delTask.getAssignee();
		String taskId = delTask.getId();
		LOGGER.info("Loan Approval Task of id " + taskId + " was assigned to " + assignedUser + ".");
		
		if (assignedUser != null) {
			LOGGER.info("Attempting to send email");
			EmailSender eS = new EmailSender("123.456.789", "username", "password", "noreply@madeup.co.nz", 25, delTask);
			eS.sendAssignmentEmail(delTask.getAssignee(), delTask.getId(), delTask.getDescription(), 
									"http://localhost:8080/camunda/app/tasklist/default/#/?searchQuery=%5B%5D&filter=5c8ee043-e39c-11e7-a065-f8b156c72bfc&sorting=%5B%7B%22sortBy%22:%22created%22,%22sortOrder%22:%22desc%22%7D%5D&task=",
										delTask.getName());
		}
	}
	
}