package uz.baraka.paynetbilling.port;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;
import uz.baraka.paynetbilling.domain.ApplicationStatus;
import uz.baraka.paynetbilling.domain.entity.Application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByApplicationId(String applicationId);

    boolean existsByApplicationIdAndStatus(String applicationId, ApplicationStatus status);

    @Query("""
      select a from Application a
      where a.userId = :userId
        and (:purpose is null or a.purpose = :purpose)
        and (:source  is null or a.source  = :source)
      order by a.createdAt desc
    """)
    List<Application> findAllByUserIdAndFilters(
            @Param("userId") UUID userId,
            @Param("purpose") ApplicationPurpose purpose,
            @Param("source") ApplicationSource source
    );
}
