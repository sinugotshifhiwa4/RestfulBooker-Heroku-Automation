# RestfulBooker-Heroku-Automation

## Overview
This project is designed for automated testing of the Restful Booker API. It uses RestAssured for API testing and supports authentication, booking creation, retrieval, update, partial update, and deletion operations.

## Prerequisites
Before running the tests, ensure the following prerequisites are met:

1. **Java:** Install JDK 8 or later.
2. **Maven:** Ensure Apache Maven is installed.
3. **Environment Configuration:** Create an environment file (.env.dev, .env.uat, etc.) with the necessary variables for encryption.
4. **Secret Key:** The secret key required for encryption will be generated dynamically during execution.

## Running Tests

### Running Encryption Tests
To run encryption-related tests, execute the following command:
```sh
mvn clean test -Dgroups=uat-encryption
```
This requires properly configured environment files with encryption keys and authentication details.

### Running API Tests
Before you run sanity, you need to ensure your authentication credentials are encrypted, so you start by creating the `envs` directory or running the generate secret key tests to create them dynamically.

To run API tests, execute:
```sh
mvn clean test -Dgroups=sanity
```
This will trigger all test cases under the `sanity` group, which includes authentication, booking operations, and validation.

## Test Cases Implemented
1. **Authentication Token Generation**
2. **Create a New Booking**
3. **Retrieve Booking Details by ID**
4. **Update Booking Details by ID**
5. **Partially Update Booking Details by ID**
6. **Delete Booking by ID**
7. **Retrieve All Bookings**

## Logging & Error Handling
- **Logger:** Log4j2 is used for logging test execution details.
- **Error Handling:** Custom `ErrorHandler` utility is implemented to log errors and handle exceptions gracefully.

## Test Data Management
Test data is managed using `TestContextStore`, which ensures test execution consistency by storing authentication tokens and booking IDs.

## Dependencies
- **RestAssured** (For API testing)
- **TestNG** (For test execution & grouping)
- **Log4j2** (For logging)
- **Dotenv** (For managing environment variables)

## Environment Configuration
Ensure your `.env` files contain:
```
AUTH_USERNAME=your_username
AUTH_PASSWORD=your_password
```
Replace values as per your test environment.

## Links
- [Detailed API Documentation](https://restful-booker.herokuapp.com/apidoc/index.html)

