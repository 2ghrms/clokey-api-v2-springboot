package org.clokey.domain.notification.repository;

import static org.clokey.notification.entity.QCodiveNotification.codiveNotification;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.notification.dto.response.NotificationListResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CodiveNotificationRepositoryImpl implements CodiveNotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<NotificationListResponse> findAllNotificationsByMemberId(
            Long memberId, Long lastNotificationId, Integer size) {
        List<NotificationListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        NotificationListResponse.class,
                                        codiveNotification.id,
                                        codiveNotification.notificationImageUrl,
                                        codiveNotification.content,
                                        codiveNotification.redirectInfo,
                                        codiveNotification.redirectType,
                                        codiveNotification.readStatus,
                                        codiveNotification.createdAt))
                        .from(codiveNotification)
                        .where(
                                codiveNotification.member.id.eq(memberId),
                                lastNotificationIdCondition(lastNotificationId))
                        .limit(size + 1)
                        .orderBy(codiveNotification.id.desc())
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression lastNotificationIdCondition(Long lastNotificationId) {
        if (lastNotificationId == null) {
            return null;
        }

        return codiveNotification.id.lt(lastNotificationId);
    }

    private <T> Slice<T> checkLastPage(int pageSize, List<T> results) {
        boolean hasNext = false;

        if (results.size() > pageSize) {
            hasNext = true;
            results.remove(pageSize);
        }

        return new SliceImpl<>(results, PageRequest.of(0, pageSize), hasNext);
    }
}
