package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "lemma")
public class LemmaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "lemma", columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private SiteModel site;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private Set<IndexModel> indexes;
}
