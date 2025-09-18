package uz.baraka.paynetbilling.application.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.baraka.paynetbilling.application.ApplicationService;
import uz.baraka.paynetbilling.config.AppProps;
import uz.baraka.paynetbilling.domain.entity.Application;
import uz.baraka.paynetbilling.domain.factory.ApplicationFactory;
import uz.baraka.paynetbilling.port.ApplicationRepository;
import uz.baraka.paynetbilling.util.IdGenerator;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository repo;
    private final AppProps props;
    private final ApplicationFactory factory;

    public ApplicationServiceImpl(ApplicationRepository repo,
                                     AppProps props,
                                     ApplicationFactory factory) {
        this.repo = repo;
        this.props = props;
        this.factory = factory;
    }

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
                req.purpose()
        );

        repo.save(app);

        String name = app.getName();
        return new CreateResponse(applicationId, name, props.fixedAmount(), req.source(), req.purpose());
    }
}
