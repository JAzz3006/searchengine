package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lemma",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"site_id", "lemma"})
        },
        indexes = {
            @Index(name = "idx_lemma_site", columnList = ("site_id, lemma"))
        }
)
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "lemma", nullable = false)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PageLemma> pageLemmasOfLemma;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        Lemma other = (Lemma) o;
        return Objects.equals(
                site != null ? site.getId() : null,
                other.site != null ? other.site.getId() : null
        ) && Objects.equals(lemma, other.lemma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, lemma);
    }
}