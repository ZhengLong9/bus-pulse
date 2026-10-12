# Database migrations

Flyway is enabled, but this bootstrap has no domain migrations. Its schema history
table is infrastructure metadata. Design and review the domain schema in
`docs/data-model.md` before adding the first `V1__description.sql` migration here.

Applied migrations are immutable; subsequent schema changes get a new version.
