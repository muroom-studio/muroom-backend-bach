package kr.muroom.muroombackendbach.subway.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "subway_stations")
public class SubwayStation {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subway_station_id_seq_gen")
  @SequenceGenerator(name = "subway_station_id_seq_gen", sequenceName = "subway_station_id_seq",
      allocationSize = 1)
  @Column(name = "subway_station_id")
  private Long id;

  @Column(length = 50, nullable = false)
  private String name;

  @Column(columnDefinition = "geography(Point, 4326)", nullable = false)
  private Point location;

  @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<SubwayStationLine> stationLines = new HashSet<>();

  public void updateLocation(Point newLocation) {
    if (!this.location.equals(newLocation)) {
      this.location = newLocation;
    }
  }

  public void addLine(SubwayLine line) {
    boolean lineExists = this.stationLines.stream()
        .anyMatch(sl -> sl.getLine().getName().equals(line.getName()));

    if (!lineExists) {
      SubwayStationLine stationLine = SubwayStationLine.create(this, line);
      this.stationLines.add(stationLine);
    }
  }
}