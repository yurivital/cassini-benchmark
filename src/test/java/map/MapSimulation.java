package map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.Arrays;

public class MapSimulation extends Simulation {

    // Load VU count from system properties
    // Reference: https://docs.gatling.io/guides/passing-parameters/
    private static final int vu = Integer.getInteger("vu", 1);

    // Define HTTP configuration
    // Reference: https://docs.gatling.io/reference/script/protocols/http/protocol/
    private static final HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080");

    // Define scenario
    // Reference: https://docs.gatling.io/reference/script/core/scenario/
    private static final ScenarioBuilder scenario = scenario("map usage").exec(
            http("Create map").post("/create").check(status().is(200)).check(currentLocationRegex("map/(.*)").saveAs("mapId"))
    ).exec(session -> {

        System.out.println("Map created with ID: " + session.getString("mapId"));
        return session;
    }).exec(
            repeat(100).on(
                    exec(session -> session.set("users", Arrays.asList("Alice", "Bob", "Tony", "Jane", "Mike", "John", "Paul", "George", "Ringo")))
                            .exec(
                                    pause(1).exec(
                                            http("Set position ")
                                                    .post("/api/v1/map/#{mapId}/position")
                                                    .asJson()
                                                    .body(StringBody("{\"nickname\": \"#{users.random()}\", \"latitude\": 49.3, \"longitude\": 1.1, \"timestamp\": #{randomLong()}}"))
                                                    .check(status().is(204))
                                    ))

            ));

    // Define assertions
    // Reference: https://docs.gatling.io/reference/script/core/assertions/
    private static final Assertion assertion = global().failedRequests().count().lt(1L);

    // Define injection profile and execute the test
    // Reference: https://docs.gatling.io/reference/script/core/injection/
    {
        //setUp(scenario.injectOpen(rampUsersPerSec(1).to(30).during(Duration.ofMinutes(10)))).assertions(assertion).protocols(httpProtocol);
        setUp(scenario.injectOpen(atOnceUsers(1))).assertions(assertion).protocols(httpProtocol);

    }
}