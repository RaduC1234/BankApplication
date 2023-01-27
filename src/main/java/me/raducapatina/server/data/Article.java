package me.raducapatina.server.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "articles")
@NamedQueries({
        @NamedQuery(name = "Article.findByTitle", query = "select a from Article a where a.title = :title")
})
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(name = "document_path", nullable = false)
    @JsonIgnore
    private String documentPath;

    @Column(name = "image_path", nullable = false)
    @JsonIgnore
    private String imagePath;

    @Transient
    private String imageBase64;

    @Transient
    private String tag;

    @Transient
    private String tagColorValue;

    @Transient
    private String content;
}
