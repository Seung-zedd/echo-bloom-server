package com.checkmate.bub.bridge.domain;

import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //? 추가로, JPA 리플렉션 때문에 기본 생성자를 자동으로 생성해준다고 함
// 다대다 관계를 단순하게 매핑해주는 브릿지 엔티티는 빌더 패턴을 사용할 필요가 없음
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id"})
)
public class UserCategoryBridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "category_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    public UserCategoryBridge(User user, Category category) {
        this.user = user;
        this.category = category;
    }
}
