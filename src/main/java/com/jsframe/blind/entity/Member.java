package com.jsframe.blind.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "membertbl")
@Data
public class Member {
  @Id
  @Column(name = "m_id", length = 20)
  private String mid;

  @Column(name="m_pwd", nullable = false, length = 100)
  private String mpwd;

  @Column(name="m_email", nullable = false, length = 100)
  private String memail;

  @Column(name="m_cname", nullable = false, length = 100)
  private String mcname;

}
