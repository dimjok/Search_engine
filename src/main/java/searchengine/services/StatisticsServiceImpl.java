package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItemDto;
import searchengine.dto.statistics.StatisticsDataDto;
import searchengine.dto.statistics.StatisticsResponseDto;
import searchengine.dto.statistics.TotalStatisticsDto;
import searchengine.model.SiteModel;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final LemmaRepository lemmaRepository;
    private final Random random = new Random();

    private final SitesList sites;

    @Override
    public StatisticsResponseDto getStatistics() {

        TotalStatisticsDto total = new TotalStatisticsDto();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItemDto> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItemDto item = new DetailedStatisticsItemDto();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = 0;
            int lemmas = 0;
            String status = "NON INDEXING";
            String error = "";
            LocalDateTime statusTime = LocalDateTime.now();
            if (siteRepository.findByUrl(site.getUrl()).isPresent()) {
                SiteModel siteModel = siteRepository.findByUrl(site.getUrl()).orElseThrow();
                int id = siteModel.getId();
                pages = pageRepository.countBySiteId(siteModel.getId());
                status = siteModel.getStatus().toString();
                error = siteModel.getLastError();
                statusTime = siteModel.getStatusTime();
                lemmas = lemmaRepository.countBySiteId(siteModel.getId());
            }
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(status);
            item.setError(error);
            item.setStatusTime(statusTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000);
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponseDto response = new StatisticsResponseDto();
        StatisticsDataDto data = new StatisticsDataDto();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
