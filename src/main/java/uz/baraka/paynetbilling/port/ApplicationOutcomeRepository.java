package uz.baraka.paynetbilling.port;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.entity.ApplicationOutcome;

import java.util.List;

public interface ApplicationOutcomeRepository extends JpaRepository<ApplicationOutcome, Long> {
    boolean existsByApplicationIdAndPurpose(String applicationId, ApplicationPurpose purpose);
    boolean existsByApplicationId(String applicationId);

    @Query("select o from ApplicationOutcome o where o.applicationId in :ids")
    List<ApplicationOutcome> findAllByApplicationIdIn(@Param("ids") List<String> ids);
}
