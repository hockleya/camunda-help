package org.camunda.bpm.subdivisionprocess;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.subdivisionincidents.SubdivisionIncident;

import java.util.logging.Logger;

//Email imports
//Camunda imports

public class EmailSender {

	private static String SenderHost = "host.madeup.com";
	private static String SenderUser = "user@madeup.com";
	private static String SenderPassword = "userPassword";
	private static String SenderEmail = "noreply@madeup.com";
	private static int SenderPort = 25;
	private static DelegateTask delegatedTask = null;

	private final static Logger LOGGER = Logger.getLogger("EMAIL-SENDER");
	
	/**
	 * A constructor to build a new object to send emails with.
	 * @param hostName The address of the host.
	 * @param hostUser The username of the host for logging in.
	 * @param hostPass The password of the host for logging in.
	 * @param hostEmail The email of the host.
	 * @param port The port that this email will be sent from.
	 */
	public EmailSender(String hostName, String hostUser, String hostPass, String hostEmail, int port, DelegateTask delTask) {
		LOGGER.info(" --- In EmailSend constructor with input: " + hostName + ", " + hostUser + ", " + hostPass + ", " + hostEmail);
		EmailSender.SenderHost = hostName;
		EmailSender.SenderUser = hostUser;
		EmailSender.SenderPassword = hostPass;
		EmailSender.SenderEmail = hostEmail;
		EmailSender.SenderPort = port;
		EmailSender.delegatedTask = delTask;
	}
	
	/**
	* Finds the email related to the given user name, and sends an email to them alerting 
	* them of a new job assigned to them.
	* @param username = The username of the receiver.
	* @param jobId The unique ID of this job.
	* @param jobDescription = A description of the job they have been assigned.
	* @param jobAddress A link to the job on Camunda.
	* @param jobType A short description of what type of job has been assigned. Eg: "Subject Properties"
	*
	* return boolean Whether or not the email sent
	*/
	public boolean sendAssignmentEmail(String username, String jobId, String jobDescription, String jobAddress, String jobType) {
		LOGGER.info(" --- In sendAssignmentEmail with input: " + username + ", " + jobId + ", " + jobDescription + ", " + jobAddress + ", " + jobType);
		Email userEmail;
		String subject = "";
		String content = "";
		//Get the email attached to this account FROM CAMUNDA FOR NOW
		userEmail = this.prepareEmail(username, jobId, jobDescription, jobAddress, jobType);
		//Check if the email was valid. If it wasn't, return.
		if (userEmail == null) {
			return false;
		}
		//Attempt to send the email. Return whether or not the email sent.
		return this.sendEmail(username, userEmail, subject, content);
	}
	
	/**
	 * /**
	* Find the email attributed with this username, and start setting it up for being sent.
	* 
	 * @param username The username for the receiver of the email.
	 * @param jobId The unique ID of this job.
	 * @param jobDescription An expaination of what the job is.
	 * @param jobAddress A link to the tasklist page.
	 * @param jobType The type of job the user has been assigned.
	 * @return Email The email of the user, with most fields filled out.
	 */
	private Email prepareEmail(String username, String jobId, String jobDescription, String jobAddress, String jobType) {
		LOGGER.info(" --- In getUserEmail with input: " + username);
		//Get the user from Camunda's identity service.
		IdentityService iS = Context.getProcessEngineConfiguration().getIdentityService();
		User user = iS.createUserQuery().userId(username).singleResult();
		//Check for valid user
		if (user != null) {
			String receiver = user.getEmail();
			if (receiver != null && !receiver.isEmpty()) {
				//Raise the name's first letters to upper case.
				String firstname = user.getFirstName();
				firstname = firstname.substring(0, 1).toUpperCase() + firstname.substring(1);
				String lastname = user.getLastName();
				lastname = lastname.substring(0, 1).toUpperCase() + lastname.substring(1);

				//Write the subject and content of the email based on the information we have. 
				String subject = "Camunda - " + jobType + " job allocated to " + firstname + " " + lastname;
				String content = "Hello " + firstname + ",\n\nA new " + jobType + " job has been allocated to you. The information is as follows:\n\n";
				content += "Description: " + jobDescription + "\n\nAccess this job here:\n" + jobAddress + jobId;
				
				Email userEmail = new SimpleEmail();
				try {
					//Initial setup, email text and sender / receiver information.
					userEmail.setMsg(content);
					userEmail.setSubject(subject);
					userEmail.addTo(receiver);
					userEmail.setFrom(SenderEmail);
				} catch (EmailException e) {
                    this.createAndLogIncident("Failed to add receiver to email: " + e);
					return null;
				}
				return userEmail;
			}
			else {
                this.createAndLogIncident("User " + username + " does not have an email on their account. Cancelling email.");
            }
		}
		else {
            this.createAndLogIncident("User " + username + " could not be found.");
        }
		return null;
	}
	
	/**
	* Send the email, it will be built using the information given.
	* @param username The user name of the receiver.
	* @param userEmail The person email address that this email will be sent to.
	* @param subject The subject this email will send with.
	* @param content The content of the email
	*
	* @return boolean Whether or not the email sent
	**/
	private boolean sendEmail(String username, Email userEmail, String subject, String content) {
		LOGGER.info(" --- In sendEmail with input, excluding userEmail: " + username + ", " + subject + ", " + content);
		try {
			//Perform email sending setup
			userEmail.setSmtpPort(EmailSender.SenderPort);
			userEmail.setHostName(SenderHost);
			userEmail.setAuthentication(SenderUser, SenderPassword);
			userEmail.setCharset("utf-8");
			
			LOGGER.info("Email info is as follows: host name; " + userEmail.getHostName() + "\nreceiver; " + userEmail.getToAddresses() + "\nsender: " + userEmail.getFromAddress() + "\nport; " + userEmail.getSmtpPort());
			//Send the email
			userEmail.send();
		} catch (EmailException e) {
            this.createAndLogIncident("Failed to send email to assigned user " + username + ": " + e);
            return false;
		}
		return true;
	}

    /**
     * Report an incident to Camunda and to the server logs, for incident tracking.
     * @param errorMessage A short message about why the task failed.
     * @return
     */
	private void createAndLogIncident(String errorMessage) {
        LOGGER.warning(errorMessage);
        SubdivisionIncident sI = new SubdivisionIncident();
        sI.createAndSubmitIncident(delegatedTask.getExecution(), "User with no email", errorMessage);
    }
}
