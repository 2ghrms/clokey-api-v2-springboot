package org.clokey.cloth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;
import org.clokey.category.entity.Category;
import org.clokey.cloth.enums.Season;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cloth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private String clothImageUrl;

    @Column(length = 1000)
    private String clothUrl;

    private String name;

    private String brand;

    @ElementCollection(targetClass = Season.class)
    @CollectionTable(name = "cloth_season", joinColumns = @JoinColumn(name = "cloth_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "season_name")
    private Set<Season> seasons = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotNull
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @Builder(access = AccessLevel.PRIVATE)
    public Cloth(
            String clothImageUrl,
            String clothUrl,
            String name,
            String brand,
            Set<Season> seasons,
            Category category,
            Member member) {
        this.clothImageUrl = clothImageUrl;
        this.clothUrl = clothUrl;
        this.name = name;
        this.brand = brand;
        this.seasons = seasons;
        this.category = category;
        this.member = member;
    }

    public static Cloth createCloth(
            String clothImageUrl,
            String clothUrl,
            String name,
            String brand,
            List<Season> seasons,
            Category category,
            Member member) {
        return Cloth.builder()
                .clothImageUrl(clothImageUrl)
                .clothUrl(clothUrl)
                .name(name)
                .brand(brand)
                .seasons(new HashSet<>(seasons))
                .category(category)
                .member(member)
                .build();
    }

    public void updateCloth(
            String clothImageUrl,
            String clothUrl,
            String name,
            String brand,
            List<Season> seasons,
            Category category) {
        this.clothImageUrl = clothImageUrl;
        this.clothUrl = clothUrl;
        this.name = name;
        this.brand = brand;
        this.seasons = new HashSet<>(seasons);
        this.category = category;
    }
}
