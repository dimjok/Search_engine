package searchengine.services;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.LemmaAnalyzer.LemmaAnalyzer;
import searchengine.repositories.LemmaRepository;

@Service
@Data
public class LemmaServiceImpl implements LemmaService {

    @Autowired
    private final LemmaAnalyzer analyzer;

    private final LemmaRepository lemmaRepository;
}
