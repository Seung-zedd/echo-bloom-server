package com.checkmate.bub.category.init;

import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        try {
            var seeds = List.of(
                    Category.builder().type(CategoryType.PROBLEM).name("일상에 활력이 없어요").build(),
                    Category.builder().type(CategoryType.PROBLEM).name("과거를 후회해요").build(),
                    Category.builder().type(CategoryType.PROBLEM).name("외롭고 소속감을 느끼기 힘들어요").build()
            );
            var toSave = seeds.stream()
                    .filter(c -> !categoryRepository.existsByTypeAndName(c.getType(), c.getName()))
                    .toList();
            if (toSave.isEmpty()) {
                log.info("더미 카테고리 이미 존재 - 초기화 스킵 (현재 {}개)", categoryRepository.count());
                return;
            }
            categoryRepository.saveAll(toSave);
            log.info("더미 카테고리 초기화 완료 - 총 {}개(+{}) 카테고리", categoryRepository.count(), toSave.size());
        } catch (Exception e) {
            log.error("더미 카테고리 초기화 실패", e);
            throw new RuntimeException("더미 카테고리 초기화 실패 - 앱 시작 중단", e);
        }
    }
}
