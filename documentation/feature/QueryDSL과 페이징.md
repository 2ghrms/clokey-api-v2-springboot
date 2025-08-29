# 📌 QueryDsl & 페이징

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

## 1. 언제 Query DSL이 사용되나요?

다음과 같은 두 상황에서 QueryDSL이 사용됩니다.

- 복잡한 Query가 필요한 상황 (native query 또는 Jpql로 해결하기 복잡)
- DTO Projection을 사용하는 경우

> 대부분의 경우 복잡한 Query가 필요한 상황은 크게 발생하지 않을 것으로 예상됩니다.
> 
> 따라서, 거의 DTO Projection에 사용된다고 생각하시면 됩니다.

## 2. DTO Projection & Paging

- DTO 프로젝션은 레이어 원칙을 역전시키기 때문에 자주 사용하는 것은 권장하지 않습니다. (repository에서 service를 의존하기 때문)
- 하지만, 다량의 데이터를 조회하는 경우 유효한 성능의 차이가 있기 떄문에 다량의 데이터를 불러오는 ```Paging```에서 제한적으로 사용하고자 합니다.

DTO 프로젝션은 ```XXXRepositoryCustom```을 구현하는 ```XXXRepositoryImpl```에서 수행합니다.

- 이 곳에서 프로젝션으로 데이터를 불러오고 ```Slice```로 반환합니다. (Slice가 더 가볍고 모바일에서는 무한 스크롤 느낌의 기능이 많아서 도입했습니다.)
- ```페이징```에서는 ```Slice Response```에 담아서 반환해 주시면 됩니다.
- 예시는 ```Comment``` 도메인에서 많이 찾아볼 수 있습니다!

⚠️ ```@QueryProjection```는 사용하지 않나요!?

```java
projections.constructor(
    CommentListResponse.class,
    comment.id,
    member.id,
    member.nickname,
    member.profileImageUrl,
    comment.content,
    Expressions.constant(false)))
```
- 이는 프로젝션이 QueryDSL에 의존하지 않기 위해서 projections.constructor를 사용하고 있습니다. 
- 추후 리팩토링 의견과 근거가 있다면 건의 부탁드립니다.
