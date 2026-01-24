package org.clokey.domain.auth.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class UniqueUtil {

    private static final String[] PREFIX_NAMES = {"미니멀한", "모던한", "케주얼한", "스트릿한"};

    private static final String[] CLOTHING_CATEGORIES = {
        "티셔츠", "니트", "스웨터", "맨투맨", "후드티", "셔츠", "블라우스", "반팔티", "나시", "청바지", "반바지", "트레이닝", "조거팬츠",
        "면바지", "슈트팬츠", "슬렉스", "레깅스", "미니스커트", "미디스커트", "롱스커트", "원피스", "투피스", "기타", "숏패딩", "아우터",
        "무스탕", "후드집업", "점퍼", "바람막이", "가죽자켓", "청자켓", "슈트", "블레이저", "가디건", "아노락", "후리스", "양털", "코트",
        "롱패딩", "패딩조끼", "기타", "신발", "가방", "모자", "머플러", "시계", "양말", "레그웨어", "주얼리", "벨트", "선글라스", "안경",
        "기타"
    };

    public String generateRandomNickname() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String prefix = PREFIX_NAMES[random.nextInt(PREFIX_NAMES.length)];
        String category = CLOTHING_CATEGORIES[random.nextInt(CLOTHING_CATEGORIES.length)];

        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        return prefix + "-" + category + "-" + uuidPart;
    }
}
