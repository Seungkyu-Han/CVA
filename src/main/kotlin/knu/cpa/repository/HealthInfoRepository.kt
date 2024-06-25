package knu.cpa.repository

import knu.cpa.model.entity.HealthInfo
import knu.cpa.model.state.HealthState
import org.springframework.data.jpa.repository.JpaRepository

interface HealthInfoRepository: JpaRepository<HealthInfo, Int> {

    //건강 상태에 따라 건강정보를 가져오는 메서드
    fun findByHealthState(healthState: HealthState): List<HealthInfo>
}