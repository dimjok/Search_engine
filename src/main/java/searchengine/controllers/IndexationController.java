package searchengine.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.services.IndexationServiceImpl;

@RestController
@RequestMapping("/api")
public class IndexationController {


    private final IndexationServiceImpl indexationService;

    public IndexationController(IndexationServiceImpl indexationService) {
        this.indexationService = indexationService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        return indexationService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing() {
        return indexationService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public ResponseEntity indexPage(@RequestParam String url) {
        return indexationService.indexPage(url);
    }
}
