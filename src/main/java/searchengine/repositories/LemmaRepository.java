package searchengine.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;

import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaModel, Integer> {
    @Override
    Optional<LemmaModel> findById(Integer id);

    Optional<LemmaModel> findByLemma(String lemma);

    Integer countBySiteId(Integer siteId);

    Optional<LemmaModel> findBySiteIdAndLemma(Integer siteId, String lemma);

    @Override
    void delete(LemmaModel entity);
}
