package searchengine.services;

import org.springframework.http.ResponseEntity;

public interface IndexationService {
    ResponseEntity startIndexing();

    ResponseEntity stopIndexing();

    void clearDB();

    ResponseEntity indexPage(String url);
}
