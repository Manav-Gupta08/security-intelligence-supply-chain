package io.secintel.platform.organization;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "organization")
class Organization {

	enum Status {

		ACTIVE, SUSPENDED

	}

	@Id
	private UUID id;

	@Column(nullable = false, unique = true)
	private String slug;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private Long version;

	protected Organization() {
	}

	UUID getId() {
		return id;
	}

	String getSlug() {
		return slug;
	}

	String getDisplayName() {
		return displayName;
	}

	Status getStatus() {
		return status;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

}
