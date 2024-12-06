package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.SearchService;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity search(@RequestParam(name="query", defaultValue="") String query,
                                 @RequestParam(name="site",  defaultValue="") String site,
                                 @RequestParam(name="offset", defaultValue="0") int offset,
                                 @RequestParam(name="limit", defaultValue="0") int limit) {
        System.out.println(query);
        System.out.println(site);
        System.out.println(offset);
        System.out.println(limit);
        return searchService.search(query, site, offset, limit);
    }

}
