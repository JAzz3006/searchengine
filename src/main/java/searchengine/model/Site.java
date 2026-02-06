package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "site")
public class Site {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Page> pages = new HashSet<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Lemma> lemmas = new HashSet<>();

    @Transient
    private String host;

    public String getHost(){
        if (host == null && url != null){
            host = extractHost(url);
        }
        return host;
    }

    private static String extractHost(String url){
        try{
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) throw new IllegalArgumentException("Invalid site url: " + url);
            return IDN.toASCII(host.toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad url " + url);
        }
    }
}