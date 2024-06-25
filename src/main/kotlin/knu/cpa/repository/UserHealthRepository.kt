package knu.cpa.repository

import knu.cpa.model.entity.User
import knu.cpa.model.entity.UserHealth
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

interface UserHealthRepository: JpaRepository<UserHealth, Int> {

    //User가 입력한 건강정보들을 조회
    fun findByUser(user: User, pageable: Pageable): List<UserHealth>

    //건강정보 리스트를 조회
    fun findByUserOrderByIdDesc(user: User, pageable: Pageable): List<UserHealth>
    // 가장 최신의 건강정보를 조회
    fun findTopByUserOrderByIdDesc(user: User): UserHealth

    //건강정보의 아이디로 건강정보를 삭제
    @Transactional
    fun deleteByIdAndUser(id: Int, user: User): Int
}