package com.zentro.feature.category.entity;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category entity with parent-child hierarchy
 * Two-level structure: root categories (parent = null) and subcategories
 */
@Entity
@Table(
    name = "categories",
    indexes = {
        @Index(name = "idx_category_public_id", columnList = "public_id", unique = true),
        @Index(name = "idx_category_name", columnList = "name"),
        @Index(name = "idx_category_parent_id", columnList = "parent_id"),
        @Index(name = "idx_category_featured", columnList = "is_featured")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "public_id", nullable = false, unique = true, length = 50)
    private String publicId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;
    
    /**
     * Parent category (null for root categories)
     * Self-referencing relationship for hierarchy
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    /**
     * Child categories
     * Don't cascade delete to prevent accidental data loss
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> children = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Check if this is a root category (has no parent)
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    /**
     * Get the number of children
     */
    public int getChildrenCount() {
        return children != null ? children.size() : 0;
    }
}
