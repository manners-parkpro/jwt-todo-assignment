import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Calls the core APIs of a server started with the README run command (./gradlew bootRun, .\gradlew.bat bootRun).
// CI runs this file with the JDK single-file launcher (java ReadmeSmoke.java) on Windows, Linux and macOS.
// The source stays ASCII-only: JDK 17 on Windows reads it with the platform encoding (windows-1252),
// so the expected Korean message is written with unicode escapes.
public class ReadmeSmoke {

	private static final String BASE_URL = "http://localhost:8080";
	private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(4);
	private static final String TODO_NOT_FOUND = "TODO\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.";
	private static final HttpClient CLIENT = HttpClient.newHttpClient();

	public static void main(String[] args) throws Exception {
		waitForServer();
		String credentials = "{\"username\":\"ci-" + System.currentTimeMillis() + "\",\"password\":\"password1234\"}";

		// Signup, login and the TODO calls succeed only if the users, todos and refresh_tokens tables were created at startup
		expect("POST /users/signup", send("POST", "/users/signup", credentials, null), 201);
		Response login = expect("POST /users/login", send("POST", "/users/login", credentials, null), 200);
		String accessToken = stringField(login.body(), "access_token");
		String refreshToken = stringField(login.body(), "refresh_token");
		expect("GET /users/me", send("GET", "/users/me", null, accessToken), 200);
		expect("GET /todos without token", send("GET", "/todos", null, null), 401);

		Response created = expect("POST /todos", send("POST", "/todos", "{\"title\":\"ci\"}", accessToken), 201);
		String todoId = numberField(created.body(), "id");
		expect("GET /todos", send("GET", "/todos", null, accessToken), 200);
		expect("PUT /todos/{id}", send("PUT", "/todos/" + todoId, "{\"title\":\"ci done\",\"completed\":true}", accessToken), 200);
		expect("DELETE /todos/{id}", send("DELETE", "/todos/" + todoId, null, accessToken), 204);
		Response notFound = expect("GET /todos/{id} after delete", send("GET", "/todos/" + todoId, null, accessToken), 404);
		if (!notFound.body().contains(TODO_NOT_FOUND)) {
			throw new AssertionError("Korean detail message is not intact: " + notFound.body());
		}

		expect("POST /users/refresh", send("POST", "/users/refresh", "{\"refresh_token\":\"" + refreshToken + "\"}", null), 200);
		System.out.println("README smoke check passed");
	}

	private static void waitForServer() throws InterruptedException {
		Instant deadline = Instant.now().plus(STARTUP_TIMEOUT);
		while (Instant.now().isBefore(deadline)) {
			try {
				if (send("GET", "/v3/api-docs", null, null).status() == 200) {
					return;
				}
			} catch (Exception e) {
				// The server is not accepting connections yet
			}
			Thread.sleep(2000);
		}
		throw new IllegalStateException("Server did not start within " + STARTUP_TIMEOUT);
	}

	private static Response send(String method, String path, String json, String accessToken) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path)).timeout(Duration.ofSeconds(30));
		if (accessToken != null) {
			builder.header("Authorization", "Bearer " + accessToken);
		}
		if (json == null) {
			builder.method(method, HttpRequest.BodyPublishers.noBody());
		} else {
			builder.header("Content-Type", "application/json")
					.method(method, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
		}
		HttpResponse<String> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		return new Response(response.statusCode(), response.body());
	}

	// Prints only the status code so that tokens never reach the public CI log
	private static Response expect(String name, Response response, int expectedStatus) {
		System.out.println(name + " -> " + response.status());
		if (response.status() != expectedStatus) {
			throw new AssertionError(name + ": expected " + expectedStatus + " but was " + response.status());
		}
		return response;
	}

	private static String stringField(String json, String name) {
		return match(json, "\"" + name + "\"\\s*:\\s*\"([^\"]+)\"", name);
	}

	private static String numberField(String json, String name) {
		return match(json, "\"" + name + "\"\\s*:\\s*(\\d+)", name);
	}

	private static String match(String json, String regex, String name) {
		Matcher matcher = Pattern.compile(regex).matcher(json);
		if (!matcher.find()) {
			throw new AssertionError("Missing field: " + name);
		}
		return matcher.group(1);
	}

	private record Response(int status, String body) {
	}
}
