package petstoreperformancetest;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class UserSimulation extends Simulation {

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl("https://petstore.swagger.io/v2") // Base URL for API requests
        .acceptHeader("application/json") // Accept header for JSON responses
        .contentTypeHeader("application/json"); // Content type for requests

    // Random Data Generator
    private String randomString(int length) {
        String source = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(source.charAt(random.nextInt(source.length())));
        }
        return sb.toString();
    }

    // Storage for Created Users using ConcurrentMap for concurrent access
    static ConcurrentMap<String, Boolean> createdUsers = new ConcurrentHashMap<>();

    // Scenario for Creating Users
    ScenarioBuilder createUserScenario = scenario("Create User Scenario")
        .exec(session -> {
            // Generate data for a new user
            String userName = randomString(8);
            String firstName = randomString(6);
            String lastName = randomString(6);
            String email = userName + "@example.com";
            String password = randomString(10);
            String phone = randomString(10);

            // Set values in the session context
            return session
                .set("userName", userName)
                .set("firstName", firstName)
                .set("lastName", lastName)
                .set("email", email)
                .set("password", password)
                .set("phone", phone);
        })
        .exec(session -> {
            // Print session variables for debugging
            System.out.println("Session Variables: "
                + "userName=" + session.getString("userName") + ", "
                + "firstName=" + session.getString("firstName") + ", "
                + "lastName=" + session.getString("lastName") + ", "
                + "email=" + session.getString("email") + ", "
                + "password=" + session.getString("password") + ", "
                + "phone=" + session.getString("phone"));
            return session;
        })
        .exec(session -> {
            // Prepare JSON body for user creation request
            String jsonBody = String.format(
                "{ \"id\": 0, \"username\": \"%s\", \"firstName\": \"%s\", \"lastName\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"phone\": \"%s\", \"userStatus\": 0 }",
                session.getString("userName"), session.getString("firstName"), session.getString("lastName"),
                session.getString("email"), session.getString("password"), session.getString("phone")
            );
            // Print request body for debugging
            System.out.println("Request Body: " + jsonBody);
            return session.set("jsonBody", jsonBody);
        })
        .exec(http("Create User Request")
            .post("/user") // HTTP POST request to create a user
            .body(StringBody(session -> session.getString("jsonBody"))) // Use the prepared JSON body
            .check(status().is(200)) // Check that the response status is 200 OK
        )
        .exec(session -> {
            // Add the username to the storage for later retrieval
            String userName = session.getString("userName");
            createdUsers.put(userName, true);
            return session;
        });

    // Scenario for Getting Users
    ScenarioBuilder getUserScenario = scenario("Get User Scenario")
        .exec(session -> {
            // Select a random username from the storage
            List<String> userNames = new ArrayList<>(createdUsers.keySet());
            String userName = userNames.get(new Random().nextInt(userNames.size()));
            // Print the URL of the request for debugging
            System.out.println("Get User Request URL: https://petstore.swagger.io/v2/user/" + userName);
            session = session.set("userName", userName);
            return session;
        })
        .exec(http("Get User Request")
            .get("/user/#{userName}") // HTTP GET request to retrieve user details
            .check(status().is(200)) // Check that the response status is 200 OK
            .check(bodyString().saveAs("getUserResponseBody")) // Save the response body for later use
        )
        .exec(session -> {
            // Print the response body for debugging
            String getUserResponseBody = session.getString("getUserResponseBody");
            System.out.println("Get User Response Body: " + getUserResponseBody);
            return session;
        });

    // Simulation Configuration
    {
        setUp(
            createUserScenario.injectOpen(
                rampUsers(20).during(45) // Inject 20 users over 45 seconds
            ).protocols(httpProtocol),
            getUserScenario.injectOpen(
                nothingFor(30), // Wait 30 seconds before starting
                constantUsersPerSec(0.5).during(30) // Then inject 15 users at a constant rate for 30 seconds
            ).protocols(httpProtocol)
        );
    }
}
