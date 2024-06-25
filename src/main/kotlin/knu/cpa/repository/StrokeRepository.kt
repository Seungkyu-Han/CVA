package knu.cpa.repository

import knu.cpa.model.entity.Stroke
import knu.cpa.model.entity.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*


interface StrokeRepository: JpaRepository<Stroke, Int> {

    //가장 최신의 Stroke 예측 데이터를 가져오는 메서드
    @Query(
        "SELECT s FROM Stroke s " +
                "LEFT JOIN s.userHealth uH " +
                "LEFT JOIN uH.user u " +
                "WHERE u = :user ORDER BY s.id desc "
    )
    fun findTopByUserOrderByIdDesc(user: User, pageable: Pageable): List<Stroke>

    //User에 따라 Stroke 예측 데이터들을 모두 가져오는 메서드
    @Query(
        "SELECT s FROM Stroke s " +
        "LEFT JOIN s.userHealth uH " +
        "LEFT JOIN uH.user u " +
        "WHERE u = :user"
    )
    fun findByUser(user: User): List<Stroke>
}