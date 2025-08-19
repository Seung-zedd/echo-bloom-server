package com.checkmate.bub.category.init;

import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
//! 프론트에서 search2.html에서 button에 name으로 하드코딩했기 때문에 변경될 때마다 이 클래스도 수정해줘야함
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeProblemCategories();
        initializeToneCategories();
    }

    private void initializeProblemCategories() {
        // Frontend search2.html의 data-value와 매칭되는 문제 카테고리들
        String[] problemNames = {
            "일상에 활력이 없어요.",                           // data-value="1"
            "계획을 끝내지 못하고 집중력이 떨어져요.",               // data-value="2"
            "걱정이 많고 불안해요.",                           // data-value="3"
            "과거를 후회해요.",                              // data-value="4"
            "스스로가 부족하게 느껴지고 자신감이 없어요.",          // data-value="5"
            "누군가를 용서하는 것이 어려워요.",                  // data-value="6"
            "외롭고 소속감을 느끼기 힘들어요.",                  // data-value="7"
            "뭘 위해 살아야 하는지 모르겠어요.",                 // data-value="8"
            "내가 모든 걸 망친 것 같고 제자리를 느껴요."         // data-value="9"
        };

        for (int i = 0; i < problemNames.length; i++) {
            Long expectedId = (long) (i + 1); // 1부터 9까지
            String problemName = problemNames[i];
            
            // 이미 존재하는지 확인
            if (categoryRepository.findById(expectedId).isEmpty()) {
                Category problemCategory = Category.builder()
                        .type(CategoryType.PROBLEM)
                        .name(problemName)
                        .build();
                
                Category savedCategory = categoryRepository.save(problemCategory);
                log.info("초기화된 문제 카테고리: ID={}, 이름={}", savedCategory.getId(), problemName);
            } else {
                log.info("문제 카테고리가 이미 존재함: ID={}, 이름={}", expectedId, problemName);
            }
        }
    }

    private void initializeToneCategories() {
        String[] toneNames = {"Joy", "Wednesday", "Zelda"};

        for (String toneName : toneNames) {
            if (!categoryRepository.existsByTypeAndName(CategoryType.TONE, toneName)) {
                Category toneCategory = Category.builder()
                        .type(CategoryType.TONE)
                        .name(toneName)
                        .build();
                
                Category savedCategory = categoryRepository.save(toneCategory);
                log.info("초기화된 톤 카테고리: ID={}, 이름={}", savedCategory.getId(), toneName);
            } else {
                log.info("톤 카테고리가 이미 존재함: 이름={}", toneName);
            }
        }
    }
}