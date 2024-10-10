package searchengine.services;

import errors.StoppingByUserError;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import response.FalseResultResponse;
import response.TrueResultResponse;
import searchengine.LemmaAnalyzer.LemmaAnalyzer;
import searchengine.config.ConnectionConfig;
import searchengine.config.SitesList;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.enums.StatusType;
import searchengine.mappers.LemmaMapper;
import searchengine.mappers.PageMapper;
import searchengine.mappers.SiteMapper;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.sitemap.SiteMapRecursive;
import searchengine.sitemap.SiteMapRunnable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Data
public class IndexationServiceImpl implements IndexationService {

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final LemmaRepository lemmaRepository;

    private final IndexRepository indexRepository;

    private final SitesList sitesList;

    private final ConnectionConfig connection;

    private CopyOnWriteArraySet<String> lemmasNames;

    private ThreadPoolExecutor threadPoolExecutor;

    private CopyOnWriteArraySet<PageDto> pagesDto;

    private CopyOnWriteArrayList linksPool;

    private final LemmaAnalyzer lemmaAnalyzer;

    @Override
    public ResponseEntity startIndexing() {
        if (threadPoolExecutor == null) {
            startFirstTime();
            return ResponseEntity.ok(new TrueResultResponse());
        } else
        if (threadPoolExecutor.getActiveCount() != 0) {
            FalseResultResponse falseResultResponse = new FalseResultResponse();
            falseResultResponse.setError("Индексация уже запущена");
            return ResponseEntity.ok(falseResultResponse);
        } else {
            startNotFirstTime();
            return ResponseEntity.ok(new TrueResultResponse());
        }
    }

    @Override
    public ResponseEntity stopIndexing() {
        if (threadPoolExecutor.getActiveCount() != 0) {
            threadPoolExecutor.shutdownNow();
            siteRepository.findAll().forEach(site -> {
                if (site.getStatus().equals(StatusType.INDEXING)) {
                    new StoppingByUserError(site, siteRepository);
                }
            }); return ResponseEntity.ok(new TrueResultResponse());
        } else {
            FalseResultResponse falseResultResponse = new FalseResultResponse();
            falseResultResponse.setError("Индексация не запущена");
            return ResponseEntity.ok(falseResultResponse);
        }
    }

    public void startFirstTime() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(sitesList.getSites().size());
        sitesList.getSites().forEach(site -> {
            pagesDto = new CopyOnWriteArraySet<>();
            linksPool = new CopyOnWriteArrayList<>();
            lemmasNames = new CopyOnWriteArraySet<>();
            threadPoolExecutor.execute(new SiteMapRunnable(site, siteRepository, pageRepository, pagesDto, linksPool,
                    connection, lemmasNames, lemmaAnalyzer, lemmaRepository));
        });
    }

    public void startNotFirstTime() {
        clearDB();
        startFirstTime();
    }

    @Override
    public void clearDB() {
        siteRepository.deleteAll();
        pageRepository.deleteAll();
    }

    @Override
    public ResponseEntity indexPage(String url) {
        AtomicReference<PageDto> pageDto = new AtomicReference<>();
        sitesList.getSites().forEach(site -> {
            if (url.contains(site.getUrl()) && siteRepository.findByUrl(site.getUrl()).isPresent()) {
                SiteDto siteDto = SiteMapper.INSTANCE.toDto(siteRepository.findByUrl(site.getUrl()).orElseThrow());
                pageDto.set(new SiteMapRecursive(siteRepository, siteDto, connection, lemmaAnalyzer).indexPage(url));
            }
        });
        if (pageDto.get() != null && pageRepository.findByPath(pageDto.get().getPath()).isPresent()) {

            PageModel pageModel = pageRepository.findByPath(pageDto.get().getPath()).orElseThrow();
            List<IndexModel> indexes = indexRepository.findByPageId(pageModel.getId());
            indexes.forEach(i -> {
                LemmaModel lemmaModel = i.getLemma();
                if (lemmaModel.getFrequency() >= 2) {
                    lemmaModel.setFrequency(lemmaModel.getFrequency() - 1);
                    lemmaRepository.save(lemmaModel);
                }
                if (lemmaModel.getFrequency() == 1) {
                    lemmaRepository.delete(lemmaModel);
                }
                indexRepository.delete(i);
            });
            pageRepository.delete(pageModel);
            PageModel pageModelNew = PageMapper.INSTANCE.toEntity(pageDto.get());
            pageRepository.save(pageModelNew);
            pageDto.get().getSiteDto().getLemmasDto().forEach(l -> {
                if (lemmaRepository.findBySiteIdAndLemma(pageDto.get().getSiteDto().getId(), l.getLemma()).isPresent()) {
                    LemmaModel lemmaModel = lemmaRepository.findBySiteIdAndLemma(pageDto.get().getSiteDto().getId(), l.getLemma()).get();
                    lemmaModel.setFrequency(lemmaModel.getFrequency() + 1);
                    lemmaRepository.save(lemmaModel);
                } else {
                    lemmaRepository.save(LemmaMapper.INSTANCE.toEntity(l));
                }
            });
            pageDto.get().getIndexesDto().forEach(i -> {
                IndexModel indexModel = new IndexModel();
                indexModel.setPage(pageRepository.findByPath(pageDto.get().getPath()).get());
                indexModel.setLemma(lemmaRepository.findBySiteIdAndLemma(pageDto.get().getSiteDto().getId(), i.getLemmaDto().getLemma()).get());
                indexModel.setRank(i.getRank());
                indexRepository.save(indexModel);
            });
            return ResponseEntity.ok(new TrueResultResponse());
        }
        if (pageDto.get() != null && pageRepository.findByPath(pageDto.get().getPath()).isEmpty()) {
            pageRepository.save(PageMapper.INSTANCE.toEntity(pageDto.get()));
            return ResponseEntity.ok(new TrueResultResponse());
        }
        FalseResultResponse falseResultResponse = new FalseResultResponse();
        falseResultResponse.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        return ResponseEntity.ok(falseResultResponse);
    }


}
