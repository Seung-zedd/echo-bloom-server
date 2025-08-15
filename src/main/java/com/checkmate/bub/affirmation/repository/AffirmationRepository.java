package com.checkmate.bub.affirmation.repository;

import com.checkmate.bub.affirmation.domain.Affirmation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffirmationRepository extends JpaRepository<Affirmation, Long> {
}
