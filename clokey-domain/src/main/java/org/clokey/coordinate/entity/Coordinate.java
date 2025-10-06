package org.clokey.coordinate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.common.model.BaseEntity;
import org.clokey.lookbook.entity.LookBook;
import org.clokey.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coordinate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 100)
    private String memo;

    @NotNull private Boolean liked;

    @NotNull private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "look_book_id")
    private LookBook lookBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @OneToMany(mappedBy = "coordinate")
    private List<CoordinateCloth> coordinateClothes = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public Coordinate(String name, String memo, String imageUrl, LookBook lookBook, Member member) {
        this.name = name;
        this.memo = memo;
        this.liked = false;
        this.imageUrl = imageUrl;
        this.lookBook = lookBook;
        this.member = member;
    }

    public static Coordinate createDailyCoordinate(String imageUrl, Member member) {
        return Coordinate.builder().name(null).memo(null).imageUrl(imageUrl).member(member).build();
    }

    public static Coordinate createCoordinateManual(
            String name, String memo, String imageUrl, Member member, LookBook lookBook) {
        return Coordinate.builder()
                .name(name)
                .memo(memo)
                .imageUrl(imageUrl)
                .member(member)
                .lookBook(lookBook)
                .build();
    }

    public void addToDailyCoordinateToLookBook(String name, String memo, LookBook lookBook) {
        this.name = name;
        this.memo = memo;
        this.lookBook = lookBook;
    }
}
