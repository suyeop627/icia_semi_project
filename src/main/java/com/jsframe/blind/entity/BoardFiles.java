package com.jsframe.blind.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "boardfiletbl")
@Data
public class BoardFiles {
  @Id
  @Column(name="f_no")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long fno;

  @ManyToOne
  @JoinColumn(name = "b_f_no")
  private Board bfno;

  @Column(name= "f_sysname", nullable = false, length = 50)
  private String fsysname;

  @Column(name="f_oriname", nullable = false, length = 50)
  private String foriname;
}
