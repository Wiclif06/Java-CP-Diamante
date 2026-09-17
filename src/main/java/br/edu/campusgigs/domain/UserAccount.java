package br.edu.campusgigs.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;
    @Column(nullable = false, length = 8)
    private String cep;
    @Column(nullable = false, length = 120)
    private String city;
    @Column(nullable = false, length = 2)
    private String uf;

    protected UserAccount() {}
    public UserAccount(String name, String email, String passwordHash, Role role, String cep, String city, String uf) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.cep = cep;
        this.city = city;
        this.uf = uf;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public String getCep() { return cep; }
    public String getCity() { return city; }
    public String getUf() { return uf; }
    public void updateAddress(String cep, String city, String uf) {
        this.cep = cep;
        this.city = city;
        this.uf = uf;
    }
}
