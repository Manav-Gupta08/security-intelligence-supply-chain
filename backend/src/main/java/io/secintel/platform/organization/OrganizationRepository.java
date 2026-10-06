package io.secintel.platform.organization;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrganizationRepository extends JpaRepository<Organization, UUID> {

	List<Organization> findAllByOrderBySlugAsc(Limit limit);

	List<Organization> findBySlugGreaterThanOrderBySlugAsc(String slug, Limit limit);

}
