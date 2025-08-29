# 📌 Repository

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

### 1. Repository 명명 규칙

- 도메인 이름에 맞게
- (도메인) + Repository

### 2. Repository Structure

- 기본적으로 JPA 레포지토리에 맞게 사용하면 됩니다.
- 다만, Query DSL을 사용해야하는 경우 다음과 같은 구조를 따릅니다.

```java
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {}
```
- XXXRepository.java
- XXXRepositoryCustom.java (인터페이스)
- XXXRepositoryImpl.java (구현체)

보통 위와 같은 구조를 "일반적"으로 사용합니다.
- 그리고 Repository에서 CustomRepository를 상속받으면 Repository만 DI해서 사용가능합니다.
