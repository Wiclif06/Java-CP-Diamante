package br.edu.campusgigs.domain;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "contracts")
public class Contract {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffer service;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private UserAccount customer;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ContractStatus status;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Contract() {}
    public Contract(ServiceOffer service, UserAccount customer) {
        this.service = service;
        this.customer = customer;
        this.status = ContractStatus.SOLICITADA;
        this.createdAt = OffsetDateTime.now();
    }
    public Long getId() { return id; }
    public ServiceOffer getService() { return service; }
    public UserAccount getCustomer() { return customer; }
    public ContractStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
