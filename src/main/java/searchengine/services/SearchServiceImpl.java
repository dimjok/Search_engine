package searchengine.services;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.LemmaAnalyzer.LemmaAnalyzer;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
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
        if (site.isEmpty()) {
            siteModels.addAll(siteRepository.findAll());
        } else {
            siteModels.add(siteRepository.findByUrl(site).orElseThrow());
        }
        for (SiteModel siteModel : siteModels) {
            List<LemmaModel> lemmaModels = new ArrayList<>();
            Set<String> queryLemmas = analyzer.getLemmaSet(query);
            queryLemmas.forEach(ql -> {
                if (lemmaRepository.findBySiteIdAndLemma(siteModel.getId(), ql).isPresent()) {
                    LemmaModel lemmaModel = lemmaRepository.findBySiteIdAndLemma(siteModel.getId(), ql).orElseThrow();
                    if (lemmaModel.getFrequency() < lemmaModel.getSite().getPages().size() * PERCENT) {
                        lemmaModels.add(lemmaModel);
                    }
                }
            });
            Comparator<LemmaModel> comparator = Comparator.comparing(l -> l.getFrequency());
            Collections.sort(lemmaModels, comparator);
            List<PageModel> pageModels = searchPages(lemmaModels);
            TreeMap<Float, PageModel> pagesAndRelevant = relevantCalculation(pageModels, lemmaModels);
        }
        return null;
    }

    public List<PageModel> searchPages(List<LemmaModel> lemmaModels) {
        HashMap<PageModel, Integer> rightPages = new HashMap<>();
        List<PageModel> pageModels = new ArrayList<>();
        for (LemmaModel lemmaModel : lemmaModels) {
            List<IndexModel> indexModels = indexRepository.findByLemmaId(lemmaModel.getId());
            for (IndexModel indexModel : indexModels) {
                if (!rightPages.containsKey(indexModel.getPage())) {
                    rightPages.put(indexModel.getPage(), 1);
                } else {
                    rightPages.replace(indexModel.getPage(), rightPages.get(indexModel.getPage()) + 1);
                }
            }
        }
        for (Map.Entry<PageModel, Integer> entry : rightPages.entrySet()) {
            if (entry.getValue() == lemmaModels.size()) {
                pageModels.add(entry.getKey());
            }
        }
        return pageModels;
    }
    public TreeMap<Float, PageModel> relevantCalculation(List<PageModel> pageModels, List<LemmaModel> lemmaModels) {
        TreeMap<Float, PageModel> pagesAndRelevant = new TreeMap<>();
        List<Float> relevant = new ArrayList<>();
        for (PageModel pageModel : pageModels) {
            Float absRelevant = 0.0F;
            for (LemmaModel lemmaModel : lemmaModels) {
                IndexModel indexModel = indexRepository.findByPageIdAndLemmaId(pageModel.getId(), lemmaModel.getId()).orElseThrow();
                absRelevant = absRelevant + indexModel.getRank();
            }
            relevant.add(absRelevant);
        }
        TreeSet<Float> sortingRelevant = new TreeSet<>();
        relevant.forEach(r -> sortingRelevant.add(r));
        for (int i = 0; i < pageModels.size(); i++) {
            pagesAndRelevant.put((relevant.get(i) / sortingRelevant.last()), pageModels.get(i));
        }
        return pagesAndRelevant;
    }
}
