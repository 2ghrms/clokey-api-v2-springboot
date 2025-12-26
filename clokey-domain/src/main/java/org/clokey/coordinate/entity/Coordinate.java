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
import org.clokey.coordinate.enums.CoordinateType;
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

    /** DEFAULT : 룩북에서 만들어진 코디 DAILY : 오늘의 코디에서 만들어진 코디 CoordinateType을 통해 코디를 구분합니다. */
    @Enumerated(EnumType.STRING)
    @NotNull
    private CoordinateType coordinateType;

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
    public Coordinate(
            String name,
            String memo,
            String imageUrl,
            CoordinateType coordinateType,
            LookBook lookBook,
            Member member) {
        this.name = name;
        this.memo = memo;
        this.liked = false;
        this.imageUrl = imageUrl;
        this.coordinateType = coordinateType;
        this.lookBook = lookBook;
        this.member = member;
    }

    /** 오늘의 코디는 LookBook에 속하지 않으며, 이름과 메모가 없습니다. */
    public static Coordinate createDailyCoordinate(String imageUrl, Member member) {
        return Coordinate.builder()
                .name(null)
                .memo(null)
                .imageUrl(imageUrl)
                .coordinateType(CoordinateType.DAILY)
                .member(member)
                .build();
    }

    /** 룩북에서 직접 코디를 만드는 생성자 */
    public static Coordinate createCoordinateManual(
            String name, String memo, String imageUrl, Member member, LookBook lookBook) {
        return Coordinate.builder()
                .name(name)
                .memo(memo)
                .imageUrl(imageUrl)
                .coordinateType(CoordinateType.DEFAULT)
                .member(member)
                .lookBook(lookBook)
                .build();
    }

    /** 오늘의 코디 -> 룩북 추가 시 필요 정보를 입력합니다. */
    public void addToDailyCoordinateToLookBook(String name, String memo, LookBook lookBook) {
        this.name = name;
        this.memo = memo;
        this.lookBook = lookBook;
    }

    public void updateCoordinate(String name, String memo, String imageUrl) {
        this.name = name;
        this.memo = memo;
        this.imageUrl = imageUrl;
    }

    /** 룩북에서 오늘의 코디를 분리해도 데이터 집계용으로 삭제하지 않습니다. 반면, DEFAULT로 만들어진 코디는 삭제해도 무방합니다. */
    public void detachDailyCoordinate() {
        this.name = null;
        this.memo = null;
        this.liked = false;
        this.lookBook = null;
    }

    public void toggleLike() {
        this.liked = !liked;
    }
}
