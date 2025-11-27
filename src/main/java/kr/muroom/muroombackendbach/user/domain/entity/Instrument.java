package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "instruments")
public class Instrument {

  @Id
  @Column(name = "instruments_id")
  private Long id;

  @Column(nullable = false)
  private String name;
}