package org.home.productivity.traverse.actuator;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

// TODO: may need to check out liveliness and readiness probes: https://www.baeldung.com/spring-liveness-readiness-probes

/**
 * Customize Spring Actuator /health endpoint.
 * <p>
 * For now, we have added checks to downstream services. If downstream services
 * are down then you're going to have a bad time.
 * See: https://springhow.com/custom-health-check-for-springboot/
 * <p>
 * Some of the /health endpoint can be customized with
 * application.properties. Check documentation there before customizing here
 * <p>
 * Normally, you don't need to include a parameter inside of the component
 * stereotype but here it is necessary so that the /health indicator can
 * differentiate custom health endpoints. By giving this component the name
 * "downstream-google" the /health endpoint will split it out as
 * "downstream-google". Otherwise, it gets a generic name and you can't tell
 * what is up or down
 */
@Component("downstream-google")
@Slf4j
public class CustomHealthIndicator implements HealthIndicator {

    @Value("${application.actuator.downstream.service.url}")
    private String downstreamUrl;

    RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        try {
            log.info("Checking health...");
            // TODO: will need a lot of work to make this more sophisticated such as:
            // 1) a means of iterating off of a collection of downstream systems
            // 2) a means of identifying the method of validating downstream system (i.e.
            // ping test checking only http status or actual reading of json)
            return checkStatusVisHttpStatus(downstreamUrl);
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }

    // check for http status
    /**
     * Checks status of downstream system utilizing only http status
     * 
     * @return
     */
    private Health checkStatusVisHttpStatus(String uri) throws Exception {
        // for more: https://www.baeldung.com/java-9-http-client
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .version(Version.HTTP_2)
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newBuilder()
                .followRedirects(Redirect.ALWAYS)
                .build()
                .send(request, BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return Health.up().build();
        } else {
            return Health.down().withDetail("status", response.statusCode()).build();
        }
    }

    /**
     * Checks status of downstream system utilizing json response
     * 
     * @return
     */
    // TODO: finish someday
    /*
     * private Health checkStatusViaJson() {
     * ResponseEntity<JsonNode> responseEntity =
     * restTemplate.getForEntity(downstreamUrl, JsonNode.class);
     * if (responseEntity.getStatusCode().is2xxSuccessful()) {
     * String status = responseEntity.getBody().get("status").textValue();
     * if (status.equals("OK")) {
     * return Health.up().withDetail("status", status).build();
     * } else {
     * return Health.down().build();
     * }
     * } else {
     * return Health.down().build();
     * }
     * }
     */
}
