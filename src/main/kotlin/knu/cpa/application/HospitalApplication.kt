package knu.cpa.application

import knu.cpa.model.dto.hospital.res.HospitalGetElementRes
import org.springframework.http.ResponseEntity

//ServiceImpl의 설명 참조
interface HospitalApplication {
    fun get(latitude: Float, longitude: Float, size: Int): ResponseEntity<List<HospitalGetElementRes>>
}