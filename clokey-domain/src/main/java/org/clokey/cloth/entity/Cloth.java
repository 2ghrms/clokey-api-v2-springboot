package org.clokey.cloth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.clokey.category.entity.Category;
import org.clokey.common.model.BaseEntity;
import org.clokey.folder.entity.ClothFolder;
import org.clokey.history.entity.HistoryCloth;
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

    private int price;

    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotNull
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @Builder(access = AccessLevel.PRIVATE)
    private Cloth(String clothImageUrl, Category category, Member member) {
        this.clothImageUrl = clothImageUrl;
        this.category = category;
        this.member = member;
    }

    public static Cloth createCloth(String clothImageUrl, Category category, Member member) {
        return Cloth.builder()
                .clothImageUrl(clothImageUrl)
                .category(category)
                .member(member)
                .build();
    }

    @OneToMany(mappedBy = "cloth")
    private List<HistoryCloth> historyClothes = new ArrayList<>();

    @OneToMany(mappedBy = "cloth")
    private List<ClothFolder> clothFolders = new ArrayList<>();
}
