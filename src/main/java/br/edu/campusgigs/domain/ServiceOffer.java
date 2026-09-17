package br.edu.campusgigs.domain;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class ServiceOffer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private UserAccount provider;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(nullable = false, columnDefinition = "text")
    private String description;
    @Column(nullable = false, length = 80)
    private String category;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ServiceStatus status;

    protected ServiceOffer() {}
    public ServiceOffer(UserAccount provider, String title, String description, String category, BigDecimal price) {
        this.provider = provider;
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.status = ServiceStatus.ATIVO;
    }
    public Long getId() { return id; }
    public UserAccount getProvider() { return provider; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public ServiceStatus getStatus() { return status; }
    public void edit(String title, String description, String category, BigDecimal price) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
    }
    public void close() { this.status = ServiceStatus.ENCERRADO; }
    public void changeStatus(ServiceStatus status) { this.status = status; }
}
