# Project Instructions

This is a Java 21 Spring Boot backend project.

## Architecture Rules

- Follow Clean Architecture and DDD boundaries.
- Keep controllers thin.
- Put business logic in application/domain services.
- Do not expose JPA entities directly from API.
- Use DTOs for request/response contracts.
- Use MapStruct when mapping is needed.
- Do not change existing public API contracts unless explicitly requested.
- Do not rename packages, endpoints, database columns, or JSON fields without confirmation.
- Preserve backward compatibility with existing migrations and data.

## Spring Boot Rules

- Use constructor injection only.
- Avoid field injection.
- Keep transactions at service/application layer.
- Use @Transactional(readOnly = true) for query use-cases.
- Avoid N+1 queries.
- Prefer projections for table/list APIs.
- Avoid loading large entity graphs for paginated tables.
- Validate input at API boundary.
- Use existing exception handling patterns.

## Database Rules

- PostgreSQL is the target DB.
- Prefer indexed filters for table APIs.
- Avoid unnecessary DISTINCT unless join duplication requires it.
- For date range filters, use inclusive start and exclusive end.
- Do not add heavy joins to paginated table APIs without explaining performance impact.

## Security Rules

- Respect organization scope and role-based visibility.
- Do not bypass access checks.
- Do not add action booleans like canView/canApprove unless explicitly requested.
- Do not log sensitive personal data, tokens, passwords, PINFL, passport data, or raw request bodies.

## Workflow Rules

- Before changing code, explain the plan.
- Make small, reviewable changes.
- After changes, show the diff summary.
- Do not run destructive commands.
- Do not run git push.
- Do not commit unless explicitly requested.

## Commands

- Build: ./mvnw clean package
- Test: ./mvnw test
- Run tests for module when possible before full build.