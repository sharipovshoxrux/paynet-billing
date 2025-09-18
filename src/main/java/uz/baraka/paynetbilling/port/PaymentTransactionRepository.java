package uz.baraka.paynetbilling.port;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.baraka.paynetbilling.domain.entity.PaymentTransaction;
import uz.baraka.paynetbilling.domain.entity.TxnState;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    boolean existsByApplicationIdAndState(String applicationId, TxnState state);
    Optional<PaymentTransaction> findTopByApplicationIdOrderByCreatedAtDesc(String applicationId);
    List<PaymentTransaction> findAllByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}

