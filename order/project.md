Senior-Level Spring Boot Take-Home
Assessment
E-Commerce Order and Inventory Microservices
Overview: Build a mini e-commerce back end consisting of multiple Spring Boot services
(e.g. Product Service and Order Service). Each service should be a standalone application
with its own database, communicating via REST or messaging. The architecture should
follow microservice principles – each service is small, independent, and can be scaled or
updated separately. Spring Boot “make[s] it easy to build and run your microservices in
production at scale”. Containerize each service with Docker (using a Dockerfile or Spring
Boot’s support).
Functional Requirements:
• Product Service: CRUD APIs for products (name, price, stock, etc.); list and detail
endpoints.
• Order Service: Endpoints to place a new order (reserve inventory items), view order
status, and cancel orders. Placing an order should decrease product stock (and
rollback on failure).
• User/Authentication: (Optional) A simple user model to authenticate requests (e.g.
customers and admins).
• Inter-Service Communication: The Order Service must call the Product Service (via
REST or messaging) to update inventory. Use synchronous REST calls or an eventdriven approach (e.g. publish an “order placed” event that the Product Service
consumes).
• Error Handling: Graceful error responses (e.g. order rejected if stock is insufficient).
• Resilience: Each service should be stateless (no in-memory state that isn’t
containerized) and handle restarts.
Technical Requirements:
• Spring Boot & JPA: Use Spring Boot with Spring Data JPA for persistence (e.g. a
relational DB per service). Spring Data JPA “makes it easy to implement JPA-based
repositories” and automatically wires repository interfaces. Define entities and use
Spring Data repositories for CRUD operations.
• Spring Web: Expose REST APIs with @RestController. Spring Boot Starter Web
provides the HTTP API framework.
• Security: Secure the APIs using Spring Security (for example, JWT authentication).
Spring Boot Starter Security provides authentication/authorization support. Use JWT
tokens or OAuth2 to protect endpoints; e.g. only authenticated users can create orders.
• Testing: Write thorough tests. Use unit tests (JUnit + Mockito) and integration tests
(with Spring’s test annotations). Spring Boot has testing annotations like
@SpringBootTest, @DataJpaTest, @WebMvcTest to load only needed parts. Ensure
business logic and API endpoints are tested (mocking external calls if needed).
• Containerization: Provide a Dockerfile for each service (or a multi-service Docker
Compose) so each can run in its own container. Follow Spring’s Docker guide as a
reference.
• Configuration Management: (Advanced) If time permits, use Spring Cloud Config
or Kubernetes secrets to manage service configuration.
• Additional: Use proper logging (SLF4J), and consider an API Gateway (e.g. Spring
Cloud Gateway) if experimenting with routing between services.
• Use PostgreSQL
Bonus Features:
• API Documentation: Integrate Swagger/OpenAPI for auto-generating API docs.
• CI/CD: Provide a CI pipeline (e.g. GitHub Actions) that builds, tests, and publishes
Docker images.
• Docker Compose: Include a docker-compose.yml to run all services (and a
database, e.g. MySQL) locally together.
• Cloud Deployment: Deploy to AWS ECS/Fargate, Azure App Service, or
Render.com (bonus for showing deployment skills).
• Observability: Add Spring Boot Actuator, health checks, and metrics.
Evaluation Criteria:
• Architecture & Clean Code: Services should be well-factored (e.g. layered
architecture, SOLID principles). The code should be clean and readable, with proper
naming and comments. Microservice boundaries should make sense.
• Correctness: Endpoints must meet requirements (e.g. product stock updates and
order placement should work as expected). Edge cases (insufficient stock, invalid
input) should be handled gracefully.
• Testing: Quality and coverage of unit/integration tests. Tests should cover critical
business logic and error cases. Proper use of Spring test annotations shows testability.
• Security: Proper use of Spring Security for authentication/authorization (e.g. JWT
setup, password hashing). Endpoints should be secured as intended.
• Containerization: Dockerfiles must correctly build images that run the service.
Services should start via docker run (or Compose) without issues.
• Documentation: Clear README with setup instructions, API details, and any design
decisions. In-code comments where non-obvious logic exists.