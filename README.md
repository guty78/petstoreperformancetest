# Pet Store Performance Test

This project contains performance tests for the Pet Store API using Gatling. The tests simulate scenarios to create users and fetch pets by status to evaluate the API's performance under load.

## Project Structure

```
src
├───main
│ └───java
│ └───petstoreperformancetest
│ ├───PetSimulation.java
│ └───UserSimulation.java
```

## Classes overview

### PetSimulation

This class simulates a scenario where pets are fetched by a random status from the Pet Store API.

#### Key Components

- **HttpProtocolBuilder**: Defines the HTTP protocol configuration for all requests, including the base URL, accept headers, and content type.
- **randomStatus**: Private method that generates a random pet status (`sold`, `available`, `pending`).
- **getPetsByRandomStatusScenario**: Scenario that performs the following steps:
    1. Generates a random status and saves it in the session.
    2. Executes an HTTP GET request to fetch pets by the generated status.
    3. Verifies that the response status is `200 OK`.
    4. Saves the response body for later use and prints it for debugging.

#### User Injection

- **constantUsersPerSec(0.5).during(60)**: Injects users at a constant rate of 0.5 users per second for 60 seconds.

### UserSimulation

This class simulates scenarios for creating users and retrieving user details from the Pet Store API.

#### Key Components

- **HttpProtocolBuilder**: Defines the HTTP protocol configuration for all requests, including the base URL, accept headers, and content type.
- **randomString**: Private method that generates a random alphanumeric string of a specified length.
- **createUserScenario**: Scenario that performs the following steps:
    1. Generates random data for a new user and saves it in the session.
    2. Prepares the JSON body for the user creation request.
    3. Executes an HTTP POST request to create the user.
    4. Verifies that the response status is `200 OK`.
    5. Stores the created username in a concurrent storage for later use.
- **getUserScenario**: Scenario that performs the following steps:
    1. Selects a random username from the previously created users.
    2. Executes an HTTP GET request to fetch user details.
    3. Verifies that the response status is `200 OK`.
    4. Saves the response body for later use and prints it for debugging.

#### User Injection

- **createUserScenario**:
    - **rampUsers(20).during(45)**: Injects 20 users gradually over 45 seconds.
- **getUserScenario**:
    - **nothingFor(30)**: Waits 30 seconds before starting the user injection.
    - **constantUsersPerSec(0.5).during(30)**: Then injects users at a constant rate of 0.5 users per second for 30 seconds.

## Running the Performance Tests

1. **Clone the repository:**

```bash
git clone https://github.com/guty78/petstoreperformancetest.git
cd petstoreperformancetest
```

2. **Run the PetSimulation test:**

```bash
mvn gatling:test -Dgatling.simulationClass=petstoreperformancetest.PetSimulation
```

3. **Run the UserSimulation test:**

```bash
mvn gatling:test -Dgatling.simulationClass=petstoreperformancetest.UserSimulation
```

## Test Scenarios

### PetSimulation
- **Fetch Pets by Random Status:**
    - Fetches pets by a randomly selected status (`sold`, `available`, `pending`).
    - Verifies that the response status is `200 OK`.
    - Prints the response body for debugging.
    - **User Injection**: Injects users at a constant rate of 0.5 users per second for 60 seconds.

### UserSimulation
- **Create User:**
    - Generates random user data.
    - Creates a new user using the Pet Store API.
    - Stores the created username for later use.
    - Verifies that the response status is `200 OK`.
    - **User Injection**: Injects 20 users gradually over 45 seconds.

- **Get User:**
    - Retrieves user details by a randomly selected username from the previously created users.
    - Verifies that the response status is `200 OK`.
    - Prints the response body for debugging.
    - **User Injection**: Waits 30 seconds before starting the user injection and then injects users at a constant rate of 0.5 users per second for 30 seconds.

## Troubleshooting

- Ensure that the Pet Store API is accessible.
- You have a stable internet connection.
- Make sure all dependencies are correctly installed by running `mvn install`.

