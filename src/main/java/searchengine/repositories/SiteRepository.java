package searchengine.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteModel, Integer> {
    @Override
    Optional<SiteModel> findById(Integer id);

    Optional<SiteModel> findByUrl(String url);

    @Override
    List<SiteModel> findAll();
}
