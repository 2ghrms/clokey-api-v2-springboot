package org.clokey.domain.category.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.TransactionUtil;
import org.clokey.category.entity.Category;
import org.clokey.domain.category.dto.response.GetCategoryListResponse;
import org.clokey.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class CategoryServiceTest extends IntegrationTest {

    @Autowired private CategoryService categoryService;
    @Autowired private CategoryRepository categoryRepository;

    @Autowired private TransactionUtil transactionUtil;

    @Nested
    class 카테고리를_조회할_때 {

        @BeforeEach
        void setUp() {
            // given (계층 구조를 가진 카테고리 엔티티 생성)
            Category top1 = Category.createCategory("상의", null);
            Category topChild1 = Category.createCategory("상의1", top1);
            Category topChild2 = Category.createCategory("상의2", top1);

            Category bottom1 = Category.createCategory("하의", null);
            Category bottomChild1 = Category.createCategory("하의1", bottom1);
            Category bottomChild2 = Category.createCategory("하의2", bottom1);

            categoryRepository.saveAll(
                    List.of(top1, topChild1, topChild2, bottom1, bottomChild1, bottomChild2));
        }

        @Test
        @Transactional
        void 유효한_요청이면_카테고리를_조회한다() {
            // when
            List<GetCategoryListResponse> result = categoryService.getCategoryList();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("상의");
            assertThat(result.get(1).name()).isEqualTo("하의");
            assertThat(result.get(0).children())
                    .extracting(GetCategoryListResponse::name)
                    .containsExactly("상의1", "상의2");
            assertThat(result.get(1).children())
                    .extracting(GetCategoryListResponse::name)
                    .containsExactly("하의1", "하의2");
        }
    }
}
