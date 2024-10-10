package searchengine.sitemap;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.apache.commons.lang3.StringUtils;
import searchengine.LemmaAnalyzer.LemmaAnalyzer;
import searchengine.config.ConnectionConfig;
import searchengine.dto.IndexDto;
import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.enums.StatusType;
import searchengine.mappers.SiteMapper;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

public class SiteMapRecursive extends RecursiveTask<Set<PageDto>> {

    private SiteMap siteMap;

    private SiteDto siteDto;

    private LemmaAnalyzer lemmaAnalyzer;

    private CopyOnWriteArraySet<String> lemmasNames;

    private CopyOnWriteArraySet<PageDto> pagesDto;

    private CopyOnWriteArrayList linksPool;

    private ConnectionConfig connection;

    private final SiteRepository siteRepository;



    public SiteMapRecursive(SiteMap siteMap, SiteDto siteDto, CopyOnWriteArraySet<PageDto> pagesDto,
                            CopyOnWriteArrayList linksPool, SiteRepository siteRepository,
                            ConnectionConfig connection, CopyOnWriteArraySet<String> lemmasNames, LemmaAnalyzer lemmaAnalyzer) {
        this.linksPool= linksPool;
        this.pagesDto = pagesDto;
        this.siteMap = siteMap;
        this.siteDto = siteDto;
        this.siteRepository = siteRepository;
        this.connection = connection;
        this.lemmasNames = lemmasNames;
        this.lemmaAnalyzer = lemmaAnalyzer;
        if (linksPool.isEmpty()) {
            linksPool.add(siteDto.getUrl());
        }
    }

    public SiteMapRecursive(SiteRepository siteRepository, SiteDto siteDto, ConnectionConfig connection, LemmaAnalyzer lemmaAnalyzer) {
        this.siteRepository = siteRepository;
        this.siteDto = siteDto;
        this.connection = connection;
        this.lemmaAnalyzer = lemmaAnalyzer;
    }

    @Override
    protected Set<PageDto> compute() {
        List<String> links = parse(siteMap.getUrl());
        links.forEach(link -> {
            if (!linksPool.contains(link)) {
                siteMap.addChildren(new SiteMap(link));
                linksPool.add(link);
            }
        });
        List<SiteMapRecursive> taskList = new ArrayList<>();
        for (SiteMap child : siteMap.getSiteMapChildren()) {
            SiteMapRecursive task = new SiteMapRecursive(child, siteDto, pagesDto, linksPool, siteRepository, connection, lemmasNames, lemmaAnalyzer);
            task.fork();
            taskList.add(task);
        }
        for (SiteMapRecursive task : taskList) {
            task.join();
        }
        return pagesDto;
    }

    public List<String> parse(String url) {
        try {
            sleep();
            List<String> links = new ArrayList<>();
            Connection.Response response = connection.connect(url);
            Document doc = response.parse();
            Elements elements = doc.select("a");
            elements.forEach(e -> {
                String link = e.absUrl("href");
                if (checkLink(link)) {
                    links.add(link);
                }
            });
            PageDto pageDto = PageDto.builder().
                    path(url).
                    code(response.statusCode()).
                    content(doc.html()).
                    indexesDto(new HashSet<>()).
                    siteDto(siteDto).
                    build();
            addLemmasAndIndexes(pageDto);
            pagesDto.add(pageDto);
            return links;
        } catch (IOException e) {
            setError(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private synchronized void sleep() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean checkLink(String link) {
        String trimLink = StringUtils.trim(link).toLowerCase();
        return StringUtils.isNotEmpty(trimLink)
                && trimLink.contains(siteDto.getUrl())
                && !trimLink.contains("#")
                && !trimLink.contains(".jpg")
                && !trimLink.contains(".jpeg")
                && !trimLink.contains(".png")
                && !trimLink.contains(".gif")
                && !trimLink.contains(".webp")
                && !trimLink.contains(".pdf")
                && !trimLink.contains(".eps")
                && !trimLink.contains(".xlsx")
                && !trimLink.contains(".doc")
                && !trimLink.contains(".pptx")
                && !trimLink.contains(".docx")
                && !trimLink.contains("?_ga");
    }

    public PageDto indexPage(String url) {
        try {
            Connection.Response response = connection.connect(url);
            Document doc = response.parse();
            siteDto.setLemmasDto(new HashSet<>());
                if (checkLink(url)) {
                    PageDto pageDto = PageDto.builder().
                            path(url.replace(siteDto.getUrl(), "")).
                            code(response.statusCode()).
                            content(doc.html()).
                            indexesDto(new HashSet<>()).
                            siteDto(siteDto).
                            build();
                    HashMap<String, Integer> lemmas = (HashMap<String, Integer>) lemmaAnalyzer.collectLemmas(pageDto.getContent());
                    for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                        LemmaDto lemmaDto = LemmaDto.builder()
                                .lemma(entry.getKey())
                                .siteDto(siteDto)
                                .build();
                        IndexDto indexDto = IndexDto.builder()
                                .pageDto(pageDto)
                                .rank(Float.valueOf(entry.getValue()))
                                .lemmaDto(lemmaDto)
                                .build();
                        siteDto.getLemmasDto().add(lemmaDto);
                        pageDto.getIndexesDto().add(indexDto);
                    }
                    return pageDto;
                }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void setError(String error) {
        siteDto.setLastError(error);
        siteDto.setStatusTime(LocalDateTime.now());
        siteDto.setStatus(StatusType.FAILED);
        siteRepository.save(SiteMapper.INSTANCE.toEntity(siteDto));
    }

    public void addLemmasAndIndexes(PageDto pageDto) {
        HashMap<String, Integer> lemmas = (HashMap<String, Integer>) lemmaAnalyzer.collectLemmas(pageDto.getContent());
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            LemmaDto lemmaDto = LemmaDto.builder()
                    .lemma(entry.getKey())
                    .siteDto(pageDto.getSiteDto())
                    .build();
            IndexDto indexDto = IndexDto.builder()
                    .pageDto(pageDto)
                    .rank(Float.valueOf(entry.getValue()))
                    .build();
            if (!lemmasNames.contains(lemmaDto.getLemma())) {
                lemmaDto.setFrequency(1);
                siteDto.getLemmasDto().add(lemmaDto);
                indexDto.setLemmaDto(lemmaDto);
                pageDto.getIndexesDto().add(indexDto);
                lemmasNames.add(lemmaDto.getLemma());
            }
            if (lemmasNames.contains(lemmaDto.getLemma())) {
                siteDto.getLemmasDto().forEach(l -> {
                    if (l.getLemma().equals(lemmaDto.getLemma())) {
                        l.setFrequency(l.getFrequency() + 1);
                        indexDto.setLemmaDto(lemmaDto);
                        pageDto.getIndexesDto().add(indexDto);
                    }
                });
            }

        }
    }
}
