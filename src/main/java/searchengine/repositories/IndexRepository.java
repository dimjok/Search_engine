package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<IndexModel, Integer> {
    @Override
    Optional<IndexModel> findById(Integer id);

    List<IndexModel> findByPageId(Integer pageId);

    List<IndexModel> findByLemmaId(Integer lemmaId);

    Optional<IndexModel> findByPageIdAndLemmaId(Integer pageId, Integer lemmaId);

    @Override
    void delete(IndexModel entity);
}
