package uz.baraka.paynetbilling.application.impl;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.baraka.paynetbilling.application.ApplicationService;
import uz.baraka.paynetbilling.config.AppProps;
import uz.baraka.paynetbilling.domain.ApplicationPurpose;
import uz.baraka.paynetbilling.domain.ApplicationSource;
import uz.baraka.paynetbilling.domain.ApplicationStatus;
import uz.baraka.paynetbilling.domain.entity.Application;
import uz.baraka.paynetbilling.domain.entity.ApplicationOutcome;
import uz.baraka.paynetbilling.domain.entity.PaymentTransaction;
import uz.baraka.paynetbilling.domain.entity.TxnState;
import uz.baraka.paynetbilling.domain.factory.ApplicationFactory;
import uz.baraka.paynetbilling.exception.ApplicationAlreadyPaidException;
import uz.baraka.paynetbilling.exception.ApplicationAlreadyProcessedException;
import uz.baraka.paynetbilling.exception.NoSuchApplicationException;
import uz.baraka.paynetbilling.exception.ResourceAccessDeniedException;
import uz.baraka.paynetbilling.port.ApplicationOutcomeRepository;
import uz.baraka.paynetbilling.port.ApplicationRepository;
import uz.baraka.paynetbilling.port.PaymentTransactionRepository;
import uz.baraka.paynetbilling.util.IdGenerator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository repo;
    private final ApplicationOutcomeRepository outcomeRepo;
    private final PaymentTransactionRepository txRepo;
    private final AppProps props;
    private final ApplicationFactory factory;

    @Override
    @Transactional
    public CreateResponse create(CreateRequest req) {
        Objects.requireNonNull(req, "request is required");
        if (req.userId() == null) throw new IllegalArgumentException("userId is required");
        if (req.pinfl() == null || req.pinfl().isBlank()) {
            throw new IllegalArgumentException("pinfl is required");
        }

        String applicationId = IdGenerator.generateApplicationId();

        Application app = factory.newApplication(
                applicationId,
                req.userId(),
                req.pinfl(),
                req.name(),
                props.fixedAmount(),
                req.source(),
                req.purpose(),
                req.bankType()
        );

        repo.save(app);

        String name = app.getName();
        return new CreateResponse(applicationId, name, props.fixedAmount(), req.source(), req.purpose());
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusResponse> findStatusesByUserId(UUID userId,
                                                                @Nullable TxnState statusFilter,
                                                                @Nullable ApplicationPurpose purpose,
                                                                @Nullable ApplicationSource source) {

        var apps = repo.findAllByUserIdAndFilters(userId, purpose, source);
        if (apps.isEmpty()) return List.of();

        var appIds = apps.stream().map(Application::getApplicationId).toList();
        var latestTxs = txRepo.findLatestByApplicationIds(appIds).stream()
                .collect(Collectors.toMap(PaymentTransaction::getApplicationId, Function.identity()));

        var results = apps.stream()
                .map(a -> {
                    var latest = latestTxs.get(a.getApplicationId());
                    if (latest == null) {
                        return new ApplicationStatusResponse(a.getApplicationId(), TxnState.CANCELLED, a.getSource(), a.getPurpose());
                    }
                    return new ApplicationStatusResponse(a.getApplicationId(), latest.getState(), a.getSource(), a.getPurpose());
                })
                .filter(r -> statusFilter == null || r.status() == statusFilter)
                .toList();

        return results;
    }

    @Transactional
    public void recordOutcome(String applicationId, ApplicationPurpose purpose) {
        repo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Клиент не найден"));

        if (!outcomeRepo.existsByApplicationIdAndPurpose(applicationId, purpose)) {
            ApplicationOutcome o = new ApplicationOutcome();
            o.setApplicationId(applicationId);
            o.setPurpose(purpose);
            outcomeRepo.save(o);
        }
    }

    @Transactional
    public boolean markPaidByBank(String applicationId) {
        var app = repo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Заявка не найдена"));

        if (app.getSource() != ApplicationSource.BANK) {
            throw new ResourceAccessDeniedException("Доступен только для заявок от Банка");
        }

        boolean alreadyPaid = txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
        if (alreadyPaid) return true;

        var tx = new PaymentTransaction();
        tx.setApplicationId(applicationId);
        tx.setAmount(props.fixedAmount());
        tx.setState(TxnState.PAID);
        txRepo.save(tx);

        return true;
    }

    @Transactional
    public void cancelApplication(String applicationId) {
        var app = repo.findByApplicationId(applicationId)
                .orElseThrow(() -> new NoSuchApplicationException("Application not found"));

        if (app.getStatus() == ApplicationStatus.CANCELLED) return;

        boolean hasPaid = txRepo.existsByApplicationIdAndState(applicationId, TxnState.PAID);
        if (hasPaid) {
            throw new ApplicationAlreadyPaidException("Application already paid, cannot cancel");
        }

        boolean hasOutcome = outcomeRepo.existsByApplicationId(applicationId);
        if (hasOutcome) {
            throw new ApplicationAlreadyProcessedException("Application already processed, cannot cancel");
        }

        app.setStatus(ApplicationStatus.CANCELLED);
        repo.save(app);
    }
}
