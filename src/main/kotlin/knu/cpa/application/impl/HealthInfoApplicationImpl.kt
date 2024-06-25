package knu.cpa.application.impl

import knu.cpa.application.HealthInfoApplication
import knu.cpa.model.dto.healthInfo.res.HealthInfoGetElementRes
import knu.cpa.model.dto.healthInfo.res.HealthInfoGetRes
import knu.cpa.model.entity.HealthInfo
import knu.cpa.model.entity.User
import knu.cpa.model.state.HealthState
import knu.cpa.repository.HealthInfoRepository
import knu.cpa.repository.StrokeRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class HealthInfoApplicationImpl(
    private val strokeRepository: StrokeRepository,
    private val healthInfoRepository: HealthInfoRepository
): HealthInfoApplication {

    //본인에게 맞는 건강정보를 조회하는 메서드
    override fun getList(authentication: Authentication): ResponseEntity<List<HealthInfoGetElementRes>> {
        val healthInfoGetElementList = mutableListOf<HealthInfoGetElementRes>()
        val strokeList = strokeRepository.findTopByUserOrderByIdDesc(User(authentication), PageRequest.of(0, 1))
        if(strokeList.isEmpty())
            return ResponseEntity(HttpStatus.NOT_FOUND)

        val stroke = strokeList.last()

        println(stroke)

        val ageFuture: CompletableFuture<List<HealthInfo>?> = CompletableFuture.supplyAsync {
            if(stroke.isAge)
                healthInfoRepository.findByHealthState(HealthState.OLD)
            else
                null
        }

        val highWeightFuture: CompletableFuture<List<HealthInfo>?> = CompletableFuture.supplyAsync {
            if(stroke.isHighWeight)
                healthInfoRepository.findByHealthState(HealthState.HIGH_WEIGHT)
            else
                null
        }

        val lowWeightFuture: CompletableFuture<List<HealthInfo>?> = CompletableFuture.supplyAsync {
            if(stroke.isLowWeight)
                healthInfoRepository.findByHealthState(HealthState.LOW_WEIGHT)
            else
                null
        }

        val heartDiseaseFuture: CompletableFuture<List<HealthInfo>?> = CompletableFuture.supplyAsync {
            if(stroke.isHighWeight)
                healthInfoRepository.findByHealthState(HealthState.HEART_DISEASE)
            else
                null
        }

        val highBloodPressureFuture: CompletableFuture<List<HealthInfo>?> = CompletableFuture.supplyAsync {
            if(stroke.isHighWeight)
                healthInfoRepository.findByHealthState(HealthState.HIGH_BLOOD_PRESSURE)
            else
                null
        }

        println(stroke.isHighWeight)
        println(stroke.isBloodPressure)

        CompletableFuture.allOf(ageFuture, highWeightFuture, lowWeightFuture, heartDiseaseFuture, highBloodPressureFuture).thenApply{
            ageFuture.get()?.map { healthInfo ->  healthInfoGetElementList.add(HealthInfoGetElementRes(healthInfo)) }
            highWeightFuture.get()?.map { healthInfo ->  healthInfoGetElementList.add(HealthInfoGetElementRes(healthInfo)) }
            lowWeightFuture.get()?.map { healthInfo ->  healthInfoGetElementList.add(HealthInfoGetElementRes(healthInfo)) }
            heartDiseaseFuture.get()?.map { healthInfo ->  healthInfoGetElementList.add(HealthInfoGetElementRes(healthInfo)) }
            highBloodPressureFuture.get()?.map { healthInfo ->  healthInfoGetElementList.add(HealthInfoGetElementRes(healthInfo)) }
        }.get()

        return ResponseEntity.ok(healthInfoGetElementList)
    }

    //해당 건강정보를 상세하게 보는 메서드
    override fun get(id: Int, authentication: Authentication): ResponseEntity<HealthInfoGetRes> {
        return ResponseEntity.ok(
            HealthInfoGetRes(healthInfoRepository.findById(id).orElseThrow())
        )
    }
}