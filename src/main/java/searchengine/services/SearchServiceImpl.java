package searchengine.services;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.LemmaAnalyzer.LemmaAnalyzer;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.SiteModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.*;

@Service
@Data
public class SearchServiceImpl implements SearchService{

    private final PageRepository pageRepository;

    private final LemmaRepository lemmaRepository;

    private final IndexRepository indexRepository;

    private final SiteRepository siteRepository;

    private final LemmaAnalyzer analyzer;

    private static final Double PERCENT = 0.70;

    @Override
    public ResponseEntity search(String query, String site, int offset, int limit) {
        List<SiteModel> siteModels = new ArrayList<>();
        System.out.println(query);
        if (site.isEmpty()) {
            siteModels.addAll(siteRepository.findAll());
        } else {
            siteModels.add(siteRepository.findByUrl(site).orElseThrow());
        }
        siteModels.forEach(System.out::println);
        for (SiteModel siteModel : siteModels) {
            List<LemmaModel> lemmaModels = new ArrayList<>();
            Set<String> queryLemmas = analyzer.getLemmaSet(query);
            System.out.println("2");
            queryLemmas.forEach(ql -> {
                if (lemmaRepository.findBySiteIdAndLemma(siteModel.getId(), ql).isPresent()) {
                    LemmaModel lemmaModel = lemmaRepository.findBySiteIdAndLemma(siteModel.getId(), ql).orElseThrow();
                    if (lemmaModel.getFrequency() < lemmaModel.getSite().getPages().size() * PERCENT) {
                        lemmaModels.add(lemmaModel);
                        System.out.println(lemmaModel.getLemma());
                    }
                }
            });
            Comparator<LemmaModel> comparator = Comparator.comparing(l -> -l.getFrequency());
            Collections.sort(lemmaModels, comparator);
            lemmaModels.forEach(System.out::println);
        }
        return null;
    }
}
