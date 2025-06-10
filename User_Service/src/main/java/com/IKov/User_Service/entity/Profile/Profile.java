package com.IKov.User_Service.entity.Profile;

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
            name = "profile_subscribers",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "subscriber_id")
    @BatchSize(size = 20)
    private List<String> subscriberList;

    @ElementCollection
    @CollectionTable(
            name = "subscribition_profiles",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "subscribition_id")
    @BatchSize(size = 20)
    private List<String> subscribitionList;

    @ElementCollection
    @CollectionTable(
            name = "profile_twitt",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "twitt_id")
    @BatchSize(size = 10)
    private List<Long> twittList;

}

