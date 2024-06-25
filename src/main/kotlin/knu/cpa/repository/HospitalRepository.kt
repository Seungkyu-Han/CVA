package knu.cpa.repository

import knu.cpa.model.entity.Hospital
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface HospitalRepository : JpaRepository<Hospital, Int> {

    //가까운 병원을 조회해주는 메서드
    @Query(
        "SELECT h FROM Hospital h " +
                "WHERE h.longitude IS NOT NULL AND h.latitude IS NOT NULL " +
                "ORDER BY " +
                "6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
                "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
                "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude))) ASC")
    fun findNearestHospitals(
        @Param("latitude") latitude: Float,
        @Param("longitude") longitude: Float,
        pageable: Pageable?
    ): List<Hospital>
}
