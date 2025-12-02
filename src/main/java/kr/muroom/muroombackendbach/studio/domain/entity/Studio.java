package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import kr.muroom.muroombackendbach.room.domain.entity.Room;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

/**
 * 스튜디오 정보를 나타내는 엔티티 클래스입니다.
 *
 * <p>스튜디오의 이름, 주소, 위치(위도/경도), 조회수, 소개, 썸네일 이미지 키, 설계도면 이미지 키 등을 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "studios")
public class Studio extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "studio_id_seq_gen")
  @SequenceGenerator(name = "studio_id_seq_gen", sequenceName = "studio_id_seq", allocationSize = 1)
  @Column(name = "studio_id")
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column
  private String address;

  /**
   * PostGIS의 GEOGRAPHY(POINT, 4326) 매핑 - Hibernate Spatial를 이용해 Point 객체로 바로 매핑됩니다.
   *
   * <p>4326은 위도/경도(WGS84) 좌표계를 의미합니다.
   */
  @Column(columnDefinition = "geography(Point, 4326)")
  private Point location;

  @Builder.Default
  @Column(name = "view_count", nullable = false)
  private Long viewCount = 0L;

  @Column(columnDefinition = "TEXT")
  private String introduction;

  @Column(length = 1024)
  private String thumbnailImageKey;

  @Column(length = 1024)
  private String blueprintImageKey;

  @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("category ASC, sequence ASC")
  @Builder.Default
  private List<StudioImage> studioImages = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private Owner owner;

  @OneToOne(mappedBy = "studio", cascade = CascadeType.ALL, orphanRemoval = true)
  StudioBuildingInfo studioBuildingInfo;

  @OneToOne(mappedBy = "studio", cascade = CascadeType.ALL, orphanRemoval = true)
  private StudioPrice studioPrice;

  @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("sequence ASC")
  @Builder.Default
  private Set<Room> rooms = new LinkedHashSet<>();

  @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("id ASC")
  @Builder.Default
  private Set<StudioForbiddenInstrument> forbiddenInstruments = new LinkedHashSet<>();
}