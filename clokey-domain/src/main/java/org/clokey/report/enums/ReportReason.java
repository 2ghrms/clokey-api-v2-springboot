package org.clokey.report.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReportReason {

    // Comment
    SWEARING_AND_CURSING(
            "욕설 및 비방이 포함되어 있습니다.", List.of("심한 욕설, 인격 모독, 명예훼손 발언 포함", "상대를 조롱하거나 비하하는 내용")),
    DISCRIMINATORY_AND_HATEFUL(
            "혐오 및 차별적 표현입니다", List.of("성별, 인종, 장애, 종교 등을 이유로 한 차별적 발언", "특정 그룹을 혐오하거나 배척하는 표현")),
    SPAM_OR_PROMOTION("스팸 홍보 및 도배 댓글입니다", List.of("반복적인 동일 댓글(광고 포함) 작성", "특정 링크를 다수 포함한 홍보성 댓글")),
    PRIVATE_INFO("사적인 정보가 포함된 댓글입니다.", List.of("본인 또는 타인의 개인정보(전화번호, 이메일, 주소 등) 포함")),
    ANNOYING_COMMENT("불쾌감을 주는 표현입니다.", List.of("조롱, 악의적인 비꼼, 공격적인 표현 포함")),
    ETC_COMMENT("기타", List.of("위 신고 항목에 해당하지 않지만, 부적절하다고 판단되는 댓글")),

    // History
    SEXUAL("음란물, 또는 선정적인 내용입니다.", List.of("성적인 묘사, 이미지·영상 포함", "노출이 과도한 사진 또는 부적절한 설명 포함")),
    VIOLENT(
            "폭력적이거나 불법적인 내용을 포함하고 있습니다",
            List.of("폭력, 학대, 자해, 살해 협박 등 위험한 행동 조장", "불법 행위를 암시하거나 조장하는 게시물 (예: 불법 약물, 도박 등)")),
    HARMFUL_TO_MINORS(
            "청소년에게 유해한 내용입니다.",
            List.of(
                    "청소년이 보기에 부적절한 주제 (예: 성인용품, 음주, 흡연 관련 내용)",
                    "자극적인 장면, 범죄 미화 등 청소년 보호법 위반 가능성이 있는 콘텐츠")),
    PRIVACY_EXPOSURE(
            "개인정보 노출 게시물입니다.",
            List.of("본인 또는 타인의 연락처, 주소, 신분증 등 개인정보가 포함된 게시물", "사적인 대화 내용이 공개된 경우")),
    ANNOYING_HISTORY(
            "악의적이거나 불쾌감을 유발하는 표현입니다.",
            List.of("특정 개인이나 집단을 비하하는 내용 (성별, 인종, 종교 차별 등)", "심한 욕설, 모욕적인 언어, 혐오 표현 포함")),
    ETC_HISTORY("기타", List.of("위 신고 항목에 해당하지 않지만, 부적절하다고 판단되는 게시물"));

    private String title;
    private List<String> contents;

    public static List<ReportReason> getCommentReportTypes() {
        return List.of(
                SWEARING_AND_CURSING,
                DISCRIMINATORY_AND_HATEFUL,
                SPAM_OR_PROMOTION,
                PRIVATE_INFO,
                ANNOYING_COMMENT,
                ETC_COMMENT);
    }

    public static List<ReportReason> getHistoryReportTypes() {
        return List.of(
                SEXUAL,
                VIOLENT,
                HARMFUL_TO_MINORS,
                PRIVACY_EXPOSURE,
                ANNOYING_HISTORY,
                ETC_HISTORY);
    }

    @JsonCreator
    public static ReportReason from(String reason) {
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(reason))
                .findFirst()
                .orElse(null);
    }
}
