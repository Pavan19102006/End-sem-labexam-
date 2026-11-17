# Direct JWT Generation — STS / Eclipse Project

## Requirements
- JDK 17 or newer (JDK 17 or 21 recommended for your IDE).
- Spring Tool Suite (STS) or Eclipse with Maven integration (m2e).
- Internet access for the initial Maven dependency download. No database required.

## Import and run in STS / Eclipse
1. Extract the ZIP file.
2. Open STS/Eclipse. Choose **File > Import > Maven > Existing Maven Projects**.
3. Browse to the extracted **jwt-generator** folder containing **pom.xml**.
4. Select the project and click **Finish**. Wait for dependencies to download.
5. In **Window > Preferences > Java > Installed JREs**, select a JDK 17 or newer.
6. If needed, right-click project > **Maven > Update Project**, then **Project > Clean**.
7. Open `src/main/java/com/example/jwt/JwtGeneratorApplication.java`.
8. Right-click > **Run As > Spring Boot App** in STS, or **Run As > Java Application** in Eclipse.
9. Wait for the console to show `Started JwtGeneratorApplication`.
10. Open http://localhost:8080/swagger-ui/index.html in your browser.

Maven creates Eclipse's project/classpath settings during import; no machine-specific IDE files are required.

## Test using Swagger UI
1. Expand **POST /api/jwt/generate**.
2. Click **Try it out**, enter the JSON below, then click **Execute**:

```json
{
  "username": "student",
  "email": "student@example.com",
  "roles": ["USER"]
}
```

3. A **200** response contains `token`, `tokenType`, `expiresIn` (3600 seconds), and `expiresAt`.
4. Copy the token value without the surrounding quotation marks.
5. Expand **POST /api/jwt/verify**, click **Try it out**, and enter:

```json
{"token": "PASTE_GENERATED_TOKEN_HERE"}
```

6. Click **Execute**. A **200** response returns the verified user claims, `sub`, `iss`, `iat`, and `exp`.
7. Change the signature or enter an invalid token: verification returns **401**. Missing username returns **400**.

## Command line alternative
From the project folder:

```sh
mvn clean verify
mvn spring-boot:run
```

Or build and start the executable JAR:

```sh
mvn clean package
java -jar target/jwt-generator-1.0.0.jar
```

## How it works
`JwtController` receives a JSON object. `JwtService` copies its custom fields into JWT claims, sets username as the subject, adds issuer and timestamps, and signs with HS256 using JJWT. The verification endpoint checks signature, issuer, and expiration before returning claims. `ApiExceptionHandler` returns JSON errors.

## Secret key configuration
By default a secure random 256-bit signing key is generated at startup. Tokens survive only until the application restarts. To keep the same key, generate a random Base64 key:

```sh
openssl rand -base64 32
```

In STS/Eclipse open **Run > Run Configurations > your application > Environment**, add `JWT_SECRET_BASE64` with that generated value, then apply and run. Never commit the key. Change lifetime using `jwt.expiration-seconds` in application.properties. Change port with `PORT` in the run environment if 8080 is occupied.

## Lab scope
This project implements the assignment's direct signing utility. The generation endpoint assumes the user has already been authenticated by a trusted caller; it does not check passwords. Do not expose this endpoint publicly as a login service: callers can supply roles. In a real microservice, protect signing behind authentication and load roles from trusted server data. JWT payloads are signed, not encrypted; do not send passwords or confidential fields.

## Troubleshooting
- **Java version error:** select JDK 17+ in IDE preferences and Run Configurations, then Maven > Update Project.
- **Red dependency markers:** check internet access; Maven > Update Project > Force Update.
- **Port already in use:** set `PORT=8081` in Run Configurations > Environment and use that port in the browser URL.
- **401 after restart:** generate a new token, or configure a persistent signing key.

## Reference documentation
- https://github.com/jwtk/jjwt
- https://springdoc.org/v2/
