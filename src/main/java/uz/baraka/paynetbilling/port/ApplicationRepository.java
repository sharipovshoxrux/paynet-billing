package uz.baraka.paynetbilling.port;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.baraka.paynetbilling.domain.entity.Application;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByApplicationId(String applicationId);
}
