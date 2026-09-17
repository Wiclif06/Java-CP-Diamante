package br.edu.campusgigs.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import br.edu.campusgigs.domain.ServiceOffer;

public interface ServiceRepository extends JpaRepository<ServiceOffer, Long> {
    @Query("select s from ServiceOffer s join fetch s.provider order by s.id desc")
    List<ServiceOffer> findAllWithProvider();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ServiceOffer s join fetch s.provider where s.id = :id")
    Optional<ServiceOffer> findLockedById(Long id);
}
