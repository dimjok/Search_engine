package searchengine.sitemap;

import lombok.AllArgsConstructor;
import searchengine.LemmaAnalyzer.LemmaAnalyzer;
import searchengine.config.ConnectionConfig;
import searchengine.config.Site;
import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.enums.StatusType;
import searchengine.mappers.IndexMapper;
import searchengine.mappers.LemmaMapper;
import searchengine.mappers.PageMapper;
import searchengine.mappers.SiteMapper;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SiteMapRunnable implements Runnable{

    private Site site;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private CopyOnWriteArraySet<PageDto> pagesDto;

    private CopyOnWriteArrayList linksPool;

    private ConnectionConfig connection;

    private CopyOnWriteArraySet<String> lemmasNames;

    private LemmaAnalyzer lemmaAnalyzer;

    private final LemmaRepository lemmaRepository;



    @Override
    public void run() {
        SiteModel siteModel = new SiteModel();
        siteModel.setUrl(site.getUrl());
        siteModel.setName(site.getName());
        setStatus(siteModel, StatusType.INDEXING);
        siteRepository.save(siteModel);
        SiteDto siteDto = SiteMapper.INSTANCE.toDto(siteModel);
        siteDto.setLemmasDto(new CopyOnWriteArraySet<>());
        SiteMap root = new SiteMap(siteDto.getUrl());
        SiteMapRecursive task = new SiteMapRecursive(root, siteDto, pagesDto, linksPool, siteRepository, connection, lemmasNames, lemmaAnalyzer);
        ForkJoinPool pool = new ForkJoinPool();
        Set<PageDto> pagesDto = pool.invoke(task);
        siteDto.setPagesDto(pagesDto);
        saveLemmas(siteDto, siteModel);
        Set<PageModel> pages = pagesDto.stream()
                .map(p -> {
                    PageModel pageModel = PageMapper.INSTANCE.toEntity(p);
                    Set<IndexModel> indexesModel = p.getIndexesDto().stream().map(i -> {
                        IndexModel indexModel = IndexMapper.INSTANCE.toEntity(i);
                        indexModel.setLemma(lemmaRepository.findBySiteIdAndLemma(siteModel.getId(),i.getLemmaDto().getLemma()).orElseThrow());
                        indexModel.setPage(pageModel);
                        return indexModel;
                    }).collect(Collectors.toSet());
                    pageModel.setIndexes(indexesModel);
                    return pageModel;
                })
                .collect(Collectors.toSet());
        if (!siteRepository.findById(siteModel.getId()).orElseThrow().getStatus().equals(StatusType.FAILED)) {
            pages.forEach(page -> {
                if (!pageRepository.findByPath(page.getPath()).isPresent()) {
                    pageRepository.save(page);
                }
            });
            setStatus(siteModel, StatusType.INDEXED);
        }
    }

    public void saveLemmas(SiteDto siteDto, SiteModel siteModel) {
        Set<LemmaDto> lemmasDto = siteDto.getLemmasDto();
        lemmasDto.forEach(l -> {
            if (lemmaRepository.findByLemma(l.getLemma()).isEmpty() ||
                    !lemmaRepository.findByLemma(l.getLemma()).orElseThrow().getSite().getId()
                            .equals(siteRepository.findByUrl(siteModel.getUrl()).orElseThrow().getId())) {
                LemmaModel lemmaModel = LemmaMapper.INSTANCE.toEntity(l);
                lemmaRepository.save(lemmaModel);
            }
        });
    }

    private void setStatus(SiteModel siteModel, StatusType type) {
        siteModel.setStatus(type);
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }
}
