# Keycloak Configuration and User Creation Guide

<a href="https://youtu.be/JtxQv18PeAw" target="_blank">Watch the video here</a>



Follow these steps to configure Keycloak and create a new user for your Spring Boot project.

## 1. Start Keycloak with Docker

Navigate to the `docker` folder in your project, where you will find the `docker-compose.yml` file.

Run the following command to start Keycloak:

```bash
docker-compose up
```

## 2. Access Keycloak Admin Console

Once Keycloak is up and running, open your browser and visit the following URL:
`http://localhost:8081/`

## 3. Create a New Realm
In the Keycloak admin console:

1. Go to the Realms section.
2. Click on Create Realm.
3. Name the realm `user-api` and click Create.


## 4. Create a New Client
Now, create a new client for the application:

1. Go to the Clients section.
2. Click on Create.
3. Name the client `userapi`, and set the Client Protocol to `openid-connect`.
4. After creating the client, go to the Settings tab and configure it as needed.
5. Valid redirect URIs: `http://localhost:4200/*`,
6. Web origins: `*`,

## 5. Create a New User
To create a new user:

1. Go to the Users section.
2. Click on Add User.
3. Fill in the details (username, email, etc.) and create the user.

## 6. Configure Client Authentication
Next, configure the `admin-cli` client:

1. Go to the Clients section and click on `admin-cli`.
2. In the Settings tab, enable Client authentication (set to On).
3. Check the Service Account Roles option.
4. Click Save.
5. After saving, additional tabs will become available.

## 7. Retrieve Client Secret
To retrieve the Client Secret:

1. Go to the Credentials tab.
2. Copy the Client Secret and paste it into your `application.yml` file under the property `keycloak.client.secret`.

## 8. Assign Service Account Roles
Assign the necessary roles to the service account:

1. Go to the Service Account Roles tab.
2. Assign the roles`manage-realm`, `manage-users` and `view-users`.

With this, your Keycloak configuration is complete.



## 9. Start Your Spring Boot Application
1. Navigate to the root directory of the Spring Boot project.

2. Run the application using Maven:

    ```bash
    ./mvnw spring-boot:run
    ```

   Alternatively, if Maven is installed globally, you can use:

    ```bash
    mvn spring-boot:run
    ```

The application should now be running locally. You can access it at `http://localhost:8080/swagger-ui/index.html#/`.


## 10. Retrieve Token from Keycloak
To authenticate and retrieve a token from Keycloak, use the following curl command:
```bash
curl --request POST \
  --url http://localhost:8081/realms/user-api/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --header 'User-Agent: insomnia/10.2.0' \
  --cookie JSESSIONID=201AB2524C288C4340F3D9277EE0D24E \
  --data client_id=userapi \
  --data username=eridani.melo@gmail.com \
  --data password=admin \
  --data grant_type=password
```

## 11. Create a New User via API
To create a new user through the Spring Boot API, use the following curl command:

```bash
curl --request POST \
  --url http://localhost:8080/api/users/create \
  --header 'Authorization: Bearer mytoken' \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/10.2.0' \
  --cookie JSESSIONID=201AB2524C288C4340F3D9277EE0D24E \
  --data '{
    "username": "newuser3",
    "firstName": "New3",
    "lastName": "User3",
    "email": "newuser3@example.com",
    "password": "strongpassword"
  }'
```
Replace `mytoken` with the actual token you retrieved in step 10.

That's it! You have now configured Keycloak, created a user, and successfully integrated it with your Spring Boot application.


## 11. Frontend ANGULAR 19