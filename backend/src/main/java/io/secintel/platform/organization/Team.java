package io.secintel.platform.organization;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "team")
class Team {

	@Id
	private UUID id;

	@Column(name = "organization_id", nullable = false, updatable = false)
	private UUID organizationId;

	@Column(nullable = false)
	private String name;

	@Column(name = "external_ref")
	private String externalRef;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private Long version;

	protected Team() {
	}

	UUID getId() {
		return id;
	}

	String getName() {
		return name;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

}
