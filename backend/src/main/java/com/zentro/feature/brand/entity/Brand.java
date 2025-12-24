package com.zentro.feature.brand.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Brand entity representing product brands in the e-commerce system
 * Features global name uniqueness and featured flag for marketing
 */
@Entity
@Table(name = "brands", indexes = {
        @Index(name = "idx_brand_public_id", columnList = "public_id", unique = true),
        @Index(name = "idx_brand_name", columnList = "name", unique = true),
        @Index(name = "idx_brand_featured", columnList = "is_featured")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 50)
    private String publicId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Future: Add relationship with Product entity
    // @OneToMany(mappedBy = "brand")
    // private List<Product> products = new ArrayList<>();
}
