package knu.cpa.application

import knu.cpa.model.dto.healthInfo.res.HealthInfoGetElementRes
import knu.cpa.model.dto.healthInfo.res.HealthInfoGetRes
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication

//ServiceImpl의 설명 참조
interface HealthInfoApplication {
    fun getList(authentication: Authentication): ResponseEntity<List<HealthInfoGetElementRes>>
    fun get(id: Int, authentication: Authentication): ResponseEntity<HealthInfoGetRes>
}