package errors;

import searchengine.enums.StatusType;
import searchengine.model.SiteModel;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;

public class StoppingByUserError {
    private SiteModel siteModel;
    private final SiteRepository siteRepository;
    private final String ERROR_TEXT = "Индексация остановлена пользователем";

    public StoppingByUserError(SiteModel siteModel, SiteRepository repository) {
        this.siteModel = siteModel;
        this.siteRepository = repository;
        setError();
    }
    private void setError() {
        siteModel.setLastError(ERROR_TEXT);
        siteModel.setStatus(StatusType.FAILED);
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }
}
