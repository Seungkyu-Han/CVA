package knu.cpa.application.impl

import knu.cpa.application.HospitalApplication
import knu.cpa.model.dto.hospital.res.HospitalGetElementRes
import knu.cpa.repository.HospitalRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class HospitalApplicationImpl(private val hospitalRepository: HospitalRepository) : HospitalApplication{

    //가까운 병원을 조회하는 메서드
    override fun get(latitude: Float, longitude: Float, size: Int): ResponseEntity<List<HospitalGetElementRes>> {
        return ResponseEntity.ok(
            hospitalRepository.findNearestHospitals(latitude, longitude, PageRequest.of(0, size)).map {
                hospital -> HospitalGetElementRes(hospital)
            }
        )
    }
}