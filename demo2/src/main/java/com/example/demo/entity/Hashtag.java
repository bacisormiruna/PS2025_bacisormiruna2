package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hashtags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hashtag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "hashtags")
    @JsonBackReference
    private Set<Post> posts = new HashSet<>();

    public Hashtag(String name) {
        this.name = name;
    }

    public Hashtag(Long id, String name) {
        this.id=id;
        this.name=name;
    }

    @Override
    public String toString() {
        return "Hashtag{id=" + id + ", name='" + name + "'}";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hashtag)) return false;
        return id != null && id.equals(((Hashtag) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}