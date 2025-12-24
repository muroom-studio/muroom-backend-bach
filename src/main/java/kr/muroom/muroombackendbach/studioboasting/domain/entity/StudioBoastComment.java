//package kr.muroom.muroombackendbach.studioboasting.domain.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.Index;
//import jakarta.persistence.Table;
//import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
//import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
//import lombok.AccessLevel;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Getter
//@Builder
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Entity
//@Table(name = "studio_boast_comments", indexes = {
//    @Index(name = "idx_comment_studio_boast_id", columnList = "studio_boast_id"),
//    @Index(name = "idx_comment_parent_id", columnList = "parent_id")
//})
//public class StudioBoastComment extends SoftDeletableEntity {
//
//  @Id
//  @Tsid
//  @Column(name = "studio_boast_comment_id")
//  private Long id;
//
//  @Column(columnDefinition = "TEXT", nullable = false)
//  private String content;
//
//  @Column(nullable = false)
//  private Long creatorUserId;
//
//  @Column
//  private Long taggedUserId;
//
//  @Column(nullable = false)
//  private Long studioBoastId;
//
//  @Column(name = "parent_id")
//  private Long parentId;
//}
