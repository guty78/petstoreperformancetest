package petstoreperformancetest;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.util.Random;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class PetSimulation extends Simulation {

	// Add the HttpProtocolBuilder:
	HttpProtocolBuilder httpProtocol =
	  http.baseUrl("https://petstore.swagger.io/v2")
	    .acceptHeader("application/json")
	    .contentTypeHeader("application/json");

    // Random Status Generator
    private String randomStatus() {
        String[] statuses = {"sold", "available", "pending"};
        Random random = new Random();
        return statuses[random.nextInt(statuses.length)];
    }
    
    // Scenario for Getting Pets by Random Status
    ScenarioBuilder getPetsByRandomStatusScenario = scenario("Get Pets By Random Status Scenario")
        .exec(session -> {
            // Generate a random status
            String status = randomStatus();
            System.out.println("Fetching pets with status: " + status);
            return session.set("status", status);
        })
        .exec(http("Get Pets Request")
            .get("/pet/findByStatus?status=#{status}") // Use the random status in the request
            .check(status().is(200)) // Check that the response status is 200 OK
            .check(bodyString().saveAs("responseBody")) // Save the response body
        )
        .exec(session -> {
            // Print the response body for debugging
            String responseBody = session.getString("responseBody");
            System.out.println("Response Body: " + responseBody);
            return session;
        });

	// Add the setUp block:
	{
	  setUp(
		  getPetsByRandomStatusScenario.injectOpen(constantUsersPerSec(0.5).during(60))
	  ).protocols(httpProtocol);
	}
	
}
