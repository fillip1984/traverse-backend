package org.home.productivity.traverse;

import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class TraverseApplication {

	@Value("${server.servlet.context-path}")
	private String serverContextPath;

	@Value("${server.port}")
	private String serverPort;

	public static void main(String[] args) {
		SpringApplication.run(TraverseApplication.class, args);
	}

	/**
	 * Logs urls of interest on start up
	 * 
	 * @param event
	 */
	@EventListener
	void onApplicationEvent(ApplicationStartedEvent event) {
		// Not my favorite solution to a problem I ran into but when I started to use
		// SampleDataRunner to load sample data this scrolled off the
		// console defeating the purpose of even outputting points of interest. Until we
		// can figure out a way to listen for CommandLineRunners to complete and switch
		// up the event we're listening for I'm just triggering the loadSampleData
		// function from here
		// event.getApplicationContext().getBean(AdminService.class).loadSampleData();

		try {
			var address = InetAddress.getLocalHost().getHostName();
			var addressBaseUrl = "http://" + address + ":" + serverPort + serverContextPath;

			log.info("Web server ready and waiting, good luck out there!\n"
					+ "\n		End points of interest:"
					+ "\n			home -> " + addressBaseUrl
					+ "\n			info -> " + addressBaseUrl + "/actuator/info"
					+ "\n			environment -> " + addressBaseUrl + "/actuator/env"
					+ "\n			health -> " + addressBaseUrl + "/actuator/health"
					+ "\n			Spring Actuator -> " + addressBaseUrl + "/actuator"
					+ "\n");
		} catch (Exception e) {
			var msg = "Exception occurred while building url to display in console on startup";
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

}
