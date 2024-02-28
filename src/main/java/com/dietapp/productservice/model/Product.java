package com.dietapp.productservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @Min(0)
    @NotNull
    private double kcal;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "product")
    private Set<CustomProperty> properties = new HashSet<>();

    @Version
    private int version;

    @CreationTimestamp
    private Instant createdDate;

    @UpdateTimestamp
    private Instant lastUpdatedDate;

    public void addProperty(CustomProperty customProperty) {
        if (properties == null) {
            properties = new HashSet<>();
        }
        properties.add(customProperty);
    }
}
