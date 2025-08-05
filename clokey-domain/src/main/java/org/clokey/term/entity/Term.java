package org.clokey.term.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.common.model.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private String title;

    @NotNull private String body;

    @NotNull private Boolean optional;
    //
    //    @Builder(access = AccessLevel.PRIVATE)
    //    private Term(String title, String body, Boolean optional) {
    //        this.title = title;
    //        this.body = body;
    //        this.optional = optional;
    //    }
    //
    //    public static Term createTerm(String title, String body, Boolean optional) {
    //        return Term.builder().title(title).body(body).optional(optional).build();
    //    }
}
