package org.clokey.domain.search.service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.history.repository.*;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.document.MemberDocument;
import org.clokey.history.entity.*;
import org.clokey.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchDocumentServiceImpl implements SearchDocumentService {

    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final MemberLikeRepository memberLikeRepository;
    private final HistoryStyleRepository historyStyleRepository;
    private final HistoryHashtagRepository historyHashtagRepository;
    private final HistoryImageRepository historyImageRepository;
    private final HistoryClothTagRepository historyClothTagRepository;

    public HistoryDocument toHistoryDocument(Long historyId) {
        History history =
                historyRepository
                        .findByIdWithMember(historyId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "History not found: " + historyId));

        Member member = history.getMember();

        // 좋아요 수 조회
        long likeCount = memberLikeRepository.countByHistoryId(historyId);

        // 첫 번째 이미지 URL
        List<HistoryImage> images = historyImageRepository.findByHistoryId(historyId);
        String historyImageUrl = images.isEmpty() ? null : images.get(0).getImageUrl();

        // 스타일 이름 리스트
        List<HistoryStyle> historyStyles = historyStyleRepository.findByHistoryId(historyId);
        List<String> styleNames =
                historyStyles.stream().map(hs -> hs.getStyle().getName()).toList();

        // 해시태그 이름 리스트
        List<HistoryHashtag> historyHashtags =
                historyHashtagRepository.findAllByHistoryIdWithHashtag(historyId);
        List<String> hashtagNames =
                historyHashtags.stream().map(hh -> hh.getHashtag().getName()).toList();

        // 카테고리 이름 리스트 (중복 제거)
        Set<String> categoryNamesSet =
                images.stream()
                        .flatMap(
                                image -> {
                                    List<HistoryClothTag> clothTags =
                                            historyClothTagRepository
                                                    .findAllByHistoryImageIdWithCloth(
                                                            image.getId());
                                    return clothTags.stream()
                                            .map(hct -> hct.getCloth().getCategory().getName());
                                })
                        .collect(Collectors.toSet());
        List<String> categoryNames =
                categoryNamesSet.stream().sorted().collect(Collectors.toList());

        HistoryDocument document = new HistoryDocument();
        document.setId(history.getId().toString());
        document.setMemberId(member.getId());
        document.setBanned(history.isBanned());
        document.setLikeCount(likeCount);
        document.setCreatedAt(
                history.getCreatedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
        document.setHistoryImageUrl(historyImageUrl);
        document.setProfileImageUrl(member.getProfileImageUrl());
        document.setNickname(member.getNickname());
        document.setStyleNames(styleNames);
        document.setHashtagNames(hashtagNames);
        document.setCategoryNames(categoryNames);

        return document;
    }

    public MemberDocument toMemberDocument(Long memberId) {
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Member not found: " + memberId));

        MemberDocument document = new MemberDocument();
        document.setId(member.getId().toString());
        document.setMemberStatus(member.getMemberStatus().name());
        document.setProfileImageUrl(member.getProfileImageUrl());
        document.setNickname(member.getNickname());

        return document;
    }
}
