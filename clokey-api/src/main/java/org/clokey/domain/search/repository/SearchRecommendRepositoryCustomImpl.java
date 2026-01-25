package org.clokey.domain.search.repository;

import static org.clokey.category.entity.QCategory.category;
import static org.clokey.cloth.entity.QCloth.cloth;
import static org.clokey.history.entity.QHashtag.hashtag;
import static org.clokey.history.entity.QHistory.history;
import static org.clokey.history.entity.QHistoryClothTag.historyClothTag;
import static org.clokey.history.entity.QHistoryHashtag.historyHashtag;
import static org.clokey.history.entity.QHistoryImage.historyImage;
import static org.clokey.history.entity.QHistoryStyle.historyStyle;
import static org.clokey.history.entity.QStyle.style;
import static org.clokey.like.entity.QMemberLike.memberLike;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.clokey.member.enums.MemberStatus;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchRecommendRepositoryCustomImpl implements SearchRecommendRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RecommendHistoryRow> findBestHistoryForUntriedStyle(
            List<Long> excludedMemberIds, Set<Long> userUsedStyleIds) {
        RecommendHistoryRow row =
                queryFactory
                        .select(
                                Projections.constructor(
                                        RecommendHistoryRow.class, history.id, style.name.min()))
                        .from(history)
                        .join(history.member, member)
                        .join(historyStyle)
                        .on(historyStyle.history.eq(history))
                        .join(historyStyle.style, style)
                        .leftJoin(memberLike)
                        .on(memberLike.history.eq(history))
                        .where(
                                history.banned.isFalse(),
                                member.memberStatus.ne(MemberStatus.BANNED),
                                excludedMemberCondition(excludedMemberIds),
                                untriedStyleCondition(userUsedStyleIds))
                        .groupBy(history.id)
                        .orderBy(memberLike.id.count().desc())
                        .limit(1)
                        .fetchOne();
        return Optional.ofNullable(row);
    }

    @Override
    public Optional<RecommendHistoryRow> findBestHistoryForCategory(
            List<Long> excludedMemberIds, String categoryName) {
        RecommendHistoryRow row =
                queryFactory
                        .select(
                                Projections.constructor(
                                        RecommendHistoryRow.class,
                                        history.id,
                                        Expressions.constant(categoryName)))
                        .from(history)
                        .join(history.member, member)
                        .join(history.historyImages, historyImage)
                        .join(historyClothTag)
                        .on(historyClothTag.historyImage.eq(historyImage))
                        .join(historyClothTag.cloth, cloth)
                        .join(cloth.category, category)
                        .leftJoin(memberLike)
                        .on(memberLike.history.eq(history))
                        .where(
                                history.banned.isFalse(),
                                member.memberStatus.ne(MemberStatus.BANNED),
                                excludedMemberCondition(excludedMemberIds),
                                category.name.eq(categoryName))
                        .groupBy(history.id)
                        .orderBy(memberLike.id.count().desc())
                        .limit(1)
                        .fetchOne();
        return Optional.ofNullable(row);
    }

    @Override
    public Optional<RecommendHistoryRow> findBestHistoryForHashtag(
            List<Long> excludedMemberIds, String hashtagName) {
        RecommendHistoryRow row =
                queryFactory
                        .select(
                                Projections.constructor(
                                        RecommendHistoryRow.class,
                                        history.id,
                                        Expressions.constant(hashtagName)))
                        .from(history)
                        .join(history.member, member)
                        .join(historyHashtag)
                        .on(historyHashtag.history.eq(history))
                        .join(historyHashtag.hashtag, hashtag)
                        .leftJoin(memberLike)
                        .on(memberLike.history.eq(history))
                        .where(
                                history.banned.isFalse(),
                                member.memberStatus.ne(MemberStatus.BANNED),
                                excludedMemberCondition(excludedMemberIds),
                                hashtag.name.eq(hashtagName))
                        .groupBy(history.id)
                        .orderBy(memberLike.id.count().desc())
                        .limit(1)
                        .fetchOne();
        return Optional.ofNullable(row);
    }

    @Override
    public Optional<String> findTopCategoryNameByHistoryIds(List<Long> memberHistoryIds) {
        if (memberHistoryIds == null || memberHistoryIds.isEmpty()) {
            return Optional.empty();
        }
        List<String> names =
                queryFactory
                        .select(category.name)
                        .from(historyClothTag)
                        .join(historyClothTag.historyImage, historyImage)
                        .join(historyClothTag.cloth, cloth)
                        .join(cloth.category, category)
                        .where(historyImage.history.id.in(memberHistoryIds))
                        .groupBy(category.id, category.name)
                        .orderBy(category.id.count().desc())
                        .limit(1)
                        .fetch();
        return names.isEmpty() ? Optional.empty() : Optional.of(names.get(0));
    }

    @Override
    public Optional<String> findMostRecentHashtagNameByMemberId(Long memberId) {
        List<String> names =
                queryFactory
                        .select(hashtag.name)
                        .from(historyHashtag)
                        .join(historyHashtag.history, history)
                        .join(historyHashtag.hashtag, hashtag)
                        .where(history.member.id.eq(memberId))
                        .orderBy(historyHashtag.createdAt.desc())
                        .limit(1)
                        .fetch();
        return names.isEmpty() ? Optional.empty() : Optional.of(names.get(0));
    }

    private BooleanExpression excludedMemberCondition(List<Long> excludedMemberIds) {
        if (excludedMemberIds == null || excludedMemberIds.isEmpty()) {
            return null;
        }
        return member.id.notIn(excludedMemberIds);
    }

    private BooleanExpression untriedStyleCondition(Set<Long> userUsedStyleIds) {
        if (userUsedStyleIds == null || userUsedStyleIds.isEmpty()) {
            return null;
        }
        return style.id.notIn(userUsedStyleIds);
    }
}
