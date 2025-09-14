# 📌 도메인 수정과 Flyway 사용법

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준 

## 📌 도메인 Convention

###  ✅엔티티 내부 순서를

- key값
- 필드
- 단방향 매핑
- 양방향 매핑 (양방향도 싹다 달아주세요)

### ✅코드 예시 → 생성 방법

```java
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private String title;

    private String coverUrl;

    @Enumerated(EnumType.STRING)
    private AlbumPlan plan;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private Subscription subscription;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorites> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Album(String title, String coverUrl, AlbumPlan plan) {
        this.title = title;
        this.coverUrl = coverUrl;
        this.plan = plan;
    }

    public static Album createAlbum(String title, String coverUrl, AlbumPlan plan) {
        return Album.builder().title(title).coverUrl(coverUrl).plan(plan).build();
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }
}

```

- 위처럼 생성 메서드는 Builder + 정적 펙토리로 만들어 주세요
- Flyway는 ```@NotNull```,```@Column```,```@Min```과 같은 어노테이션을 무시하지만, 문서화를 위해서 엔티티에도 명시 부탁드립니다.
- 또 다른 이유로는 test가 ```H2``` 데이터 베이스를 기준으로 하기 때문에 null 방어 로직을 테스트에서 테스트 하기 위함입니다. (flyway 스크립트의 제약조건을 H2가 인식 못함.)
- 이 부분은 Flyway script에도 작성될 예정이지만, Flyway가 컬럼 제약까지 확인을 하지 않기 때문에 꼼꼼히 부탁드립니다.

### ✅Enum은 In 문법으로 검사하기

<img width="707" height="94" alt="스크린샷 2025-08-28 오후 6 15 42" src="https://github.com/user-attachments/assets/7045a91b-9cd8-4d3f-b09d-4b1ca861aab2" />

###  ✅ Flyway로 DB를 생성하고 수정하려는 경우

- 수정 사항을 반영하고 다시 Application을 돌리는 경우 V1__init.sql이 이미 적용이 되었기 때문에 init 스크립트를 바꿔서 다시 적용해도 DB가 바뀌지 않아요!
- 초기 개발 단계에서는 init.sql을 수정할 예정입니다. 
- 나중에 운영중에 생기는 차이나 인덱스등을 부여하는 최적화 작업에서 Version 관리 예정.

<img width="198" height="85" alt="스크린샷 2025-08-28 오후 6 19 57" src="https://github.com/user-attachments/assets/76920512-564b-48e6-a3a9-b2f8a829d66d" />

- 이와 같은 경우 DB 스키마는 남겨두고 (또 DB를 만드는건 귀찮으니까 조심하세요) 내부 테이블만 Drop하고 다시 실행하면 init 스크립트가 실행됩니다.

## 📌 Flyway Convention
### ✅ 명명규칙
<img width="873" height="167" alt="flyway-naming" src="https://github.com/user-attachments/assets/05dcec64-02a9-431f-bf51-1c2d9cabdde8" />

네이밍순서 : Prefix → Version → Separator(__) → Description → Suffix

네이밍 예시 : V2__Add_new_table.sql

- Prefix
    - V : Version - 새로운 버전으로 업데이트 하는 경우 사용
    - U : Undo - 이전 버전으로 돌아가는 경우 사용 (유료버전이라 해당 없을 듯!!)
    - R : Repeatable - 버전에 관계 없이 반복적으로 실행되는 경우 사용
- Version

  R의 경우에는 Version 없습니다.

    - 1, 2, 3 식으로 Version 설정 (항상 이전보다 크게 설정해야!)
    - 20250725 (날짜)로 Version 설정

  이 두 가지 경우 중 설정하면 될 것 같습니다!!

- Seperator

  언더바 두개 입니다. __

- Description
    - 자유롭게 적을 수 있고, 단어 구분은 언더바로 합니다. (카멜X)
    - 구체적으로 적어주세요!

### ✅ 버전 기준

| 상황 | 예시 |
| --- | --- |
| 새로운 테이블 추가 | `V2__create_orders_table.sql` |
| 컬럼 추가/삭제 | `V3__add_email_to_users.sql` |
| 인덱스 추가 | `V4__add_index_on_email.sql` |
| 컬럼 타입 변경 | `V5__change_price_to_decimal.sql` |
| 제약조건 추가 | `V6__add_foreign_key.sql` |

단순 데이터 삽입이나 오타 수정 등은 버전 업데이트에 영향을 미치지 않습니다 !!
