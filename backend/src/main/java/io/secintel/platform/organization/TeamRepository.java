package io.secintel.platform.organization;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

interface TeamRepository extends JpaRepository<Team, UUID> {

	List<Team> findByOrganizationIdOrderByNameAsc(UUID organizationId, Limit limit);

	List<Team> findByOrganizationIdAndNameGreaterThanOrderByNameAsc(UUID organizationId, String name, Limit limit);

}
