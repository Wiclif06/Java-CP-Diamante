package br.edu.campusgigs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.campusgigs.domain.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long> {}
