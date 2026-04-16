package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "physical_books")
@PrimaryKeyJoinColumn(name = "isbn")
public class PhysicalBook extends Book {
    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    public PhysicalBook() {}

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
}
