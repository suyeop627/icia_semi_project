package com.jsframe.blind.repository;

import com.jsframe.blind.entity.Member;
import org.springframework.data.repository.CrudRepository;

public interface MemberRepository extends CrudRepository<Member, String> {

}
