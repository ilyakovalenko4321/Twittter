package com.IKov.AuthService.entity.Profile;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Data
@Table(name = "profile")
@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String tag;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String password;
    private String description;

    @ElementCollection
    @CollectionTable(
            name = "profile_twitt",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "twitt_id")
    @BatchSize(size = 10)
    private List<Long> twittList;

}

