package searchengine.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Data
@Table(name = "indexes")
public class IndexModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "rank_index", nullable = false)
    private float rank;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private PageModel page;

    @ManyToOne
    @JoinColumn(name = "lemma_id")
    private LemmaModel lemma;
}
