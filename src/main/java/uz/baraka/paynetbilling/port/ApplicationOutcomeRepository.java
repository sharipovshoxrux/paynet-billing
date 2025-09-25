package uz.baraka.paynetbilling.port;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.entity.ApplicationOutcome;

public interface ApplicationOutcomeRepository extends JpaRepository<ApplicationOutcome, Long> {
    boolean existsByApplicationIdAndPurpose(String applicationId, ApplicationPurpose purpose);
    boolean existsByApplicationId(String applicationId);
}
