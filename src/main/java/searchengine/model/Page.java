package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "page")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private  Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "path", nullable = false, columnDefinition = "TEXT")
    private String path;

    @Column(name = "code", nullable = false)
    private int code;

    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PageLemma> pageLemmasOfPage;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        Page page = (Page) o;
        return id != null && id.equals(page.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}