package org.clokey.category.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Builder(access = AccessLevel.PRIVATE)
    private Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    public static Category createCategory(String name, Category parent) {
        return Category.builder().name(name).parent(parent).build();
    }
}
