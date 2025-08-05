package org.clokey.history.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, unique = true)
    @NotNull
    private String name;

    //    @Builder(access = AccessLevel.PRIVATE)
    //    private Hashtag(String name) {
    //        this.name = name;
    //    }
    //
    //    public static Hashtag createHashtag(String name) {
    //        return Hashtag.builder().name(name).build();
    //    }
}
