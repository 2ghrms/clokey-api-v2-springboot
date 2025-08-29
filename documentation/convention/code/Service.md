# 📌 Service

> 최종 수정 일자 : 8월 28일
>
> 최종 수정자 : 나용준

### 1. Controller 명명 규칙

- 도메인 이름에 맞게
- (도메인) + Service 
- 인터페이스와 구현체 구조

### 2. Controller Structure

상단부
```
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothServiceImpl implements ClothService {

    private final MemberUtil memberUtil;

```
- 기본적으로 맨 위에 ```@Transactional(readOnly = true)```를 붙여주세요. 이는 ```@Transactional```을 걸었을 경우 조회만 하는 메서드에서 실수로 트랜잭션을 빼먹는 것을 방지합니다.
- Security 구조상 현재 유저는 ```MemberUtil```을 통해서 받아오기 때문에 일반적으로 고정되는 구조입니다.

```
@Override
public SliceResponse<ReplyListResponse> getCommentReplies(
        Long commentId, Long lastReplyId, int size, SortDirection direction) {
    final Member currentMember = memberUtil.getCurrentMember();
    final Comment comment = getCommentById(commentId);

    validateHistoryAuthority(currentMember, comment.getHistory());

    Slice<ReplyListResponse> result =
            replyRepository.findAllByCommentId(commentId, lastReplyId, size, direction);

    return SliceResponse.from(result);
}

private void validateHistoryAuthority(Member member, History history) {
    if (history.getMember().getVisibility() == Visibility.PRIVATE
            && !history.getMember().getId().equals(member.getId())) {
        throw new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY);
    }
}

private Comment getCommentById(Long commentId) {
    return commentRepository
            .findById(commentId)
            .orElseThrow(() -> new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND));
}
```
- ```Slice Repsonse```는 페이징 관련 기능 문서에서 설명되어있습니다.
- ```final``` : DB에서 가져오는 인스턴스중 바뀌지 않는다면 final을 붙여주세요.
- ```getXXX``` : ```Repository```에서는 ```findByXXX```를 쓰고 있습니다. 서비스 메서드에서 에러 처리를 해서 가져오는 메서드는 ```getXXX```로 부탁드립니다.
- 별도로 해당 에러를 처리하고 가져오는 ```Adapter```클래스를 분리하지는 않았습니다. 모든 서비스에서 조금 중복되더라도 서비스 코드에서 존재해야할 로직이라고 생각했습니다.
- ```validate 명명 규칙``` : ```validate``` + ```주제``` + ```행위 또는 상황```
- validation은 하나당 주제 하나만을 다루는 것을 권장합니다.
- validation에서 Id를 다수로 받을 경우 이름과 순서를 동일하게 해주세요 (많이 실수함).

코드 배치
```java

@Override
@Transactional
public ReplyCreateResponse createReply(Long commentId, ReplyCreateRequest request) {
    final Member currentMember = memberUtil.getCurrentMember();
    final Comment comment = getCommentById(commentId);

    validateHistoryAuthority(currentMember, comment.getHistory());

    Reply reply = Reply.createReply(request.content(), currentMember, comment);

    try {
        replyRepository.save(reply);
    } catch (DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();

        if (message != null && message.contains("fk_reply_comment")) {
            throw new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        throw e;
    }

    return ReplyCreateResponse.from(reply);
}

@Override
public SliceResponse<CommentListResponse> getHistoryComments(
        Long historyId, Long lastCommentId, int size, SortDirection direction) {
    final Member currentMember = memberUtil.getCurrentMember();
    final History history = getHistoryById(historyId);

    validateHistoryAuthority(currentMember, history);

    Slice<CommentListResponse> result =
            commentRepository.findAllByHistoryId(historyId, lastCommentId, size, direction);
    return SliceResponse.from(result);
}

@Override
public SliceResponse<ReplyListResponse> getCommentReplies(
        Long commentId, Long lastReplyId, int size, SortDirection direction) {
    final Member currentMember = memberUtil.getCurrentMember();
    final Comment comment = getCommentById(commentId);

    validateHistoryAuthority(currentMember, comment.getHistory());

    Slice<ReplyListResponse> result =
            replyRepository.findAllByCommentId(commentId, lastReplyId, size, direction);

    return SliceResponse.from(result);
}

private void validateHistoryAuthority(Member member, History history) {
    if (history.getMember().getVisibility() == Visibility.PRIVATE
            && !history.getMember().getId().equals(member.getId())) {
        throw new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY);
    }
}

private History getHistoryById(Long historyId) {
    return historyRepository
            .findById(historyId)
            .orElseThrow(() -> new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND));
}

private Comment getCommentById(Long commentId) {
    return commentRepository
            .findById(commentId)
            .orElseThrow(() -> new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND));
}

```
- 메서드
- validation
- get메서드

순으로 배치 부탁드립니다.

❓왜 ```BaseCustomException```을 바로 던지고 Custom Error를 던지지 않나요?
- 예를 들어서 댓글 API에서 기록 자체를 찾지 못하는, 서브 도메인에서 메인 도메인의 에러가 터지는 경우가 존재했습니다.
- 이 경우 ```.orElseThrow(() -> new HistoryException(CommentErrorCode.COMMENT_NOT_FOUND));``` 다음과 같은 구조를 가지게 됩니다.
- 이런 경우의 수가 매우 많기도 하고 별도의 에러 분기에 따른 처리가 없기 때문에, 현재로써는 ```BaseCustomException```를 활용합니다.
