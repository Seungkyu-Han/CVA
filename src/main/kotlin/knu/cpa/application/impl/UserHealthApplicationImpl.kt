package knu.cpa.application.impl

import com.fasterxml.jackson.databind.ObjectMapper
import knu.cpa.application.UserHealthApplication
import knu.cpa.model.dto.stroke.req.StrokeGetReq
import knu.cpa.model.dto.stroke.res.StrokeGetRes
import knu.cpa.model.dto.userHealth.req.UserHealthPostReq
import knu.cpa.model.dto.userHealth.res.UserHealthGetElementRes
import knu.cpa.model.dto.userHealth.res.UserHealthGetRes
import knu.cpa.model.entity.Stroke
import knu.cpa.model.entity.User
import knu.cpa.model.entity.UserHealth
import knu.cpa.repository.StrokeRepository
import knu.cpa.repository.UserHealthRepository
import knu.cpa.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.Period
import java.util.concurrent.CompletableFuture


open class UserHealthApplicationImpl(
    private val userHealthRepository: UserHealthRepository,
    private val strokeRepository: StrokeRepository,
    private val userRepository: UserRepository,
    @Value("\${fastApi}")
    private val fastApiServer: String,
    private val objectMapper: ObjectMapper){

    //유저 건강정보를 저장하고, 비동기적으로 확률을 예측하는 FastAPI를 호출하는 메서드
    fun post(userHealthPostReq: UserHealthPostReq, userId: Long): ResponseEntity<HttpStatus> {
        val userHealth = userHealthRepository.save(UserHealth(userHealthPostReq, User(userId)))

        var age = 0
        var bmi = 0F

        CompletableFuture.supplyAsync {
            println("STEP1")
            userRepository.findById(userId).orElseThrow()
        }.thenApplyAsync {
            println("STEP2")
            age = Period.between(it.birthday, LocalDate.now()).years
            bmi = (userHealthPostReq.weight)/((userHealthPostReq.height * userHealthPostReq.height) / 10000)
            objectMapper.writeValueAsString(StrokeGetReq(it, userHealthPostReq))
        }.thenApply {
            println("STEP3")
            val httpHeaders = HttpHeaders()
            httpHeaders.contentType = MediaType.APPLICATION_JSON
            HttpEntity(it, httpHeaders)
        }.thenApplyAsync {
            println("STEP4")
            val postForEntity = RestTemplate().postForEntity("$fastApiServer/predict_stroke", it, StrokeGetRes::class.java)
            println(postForEntity)
            postForEntity
        }.thenApplyAsync {
            println("STEP5\n")
            println(strokeRepository.save(
                Stroke(id = userHealth.id,
                    userHealth = userHealth,
                    probability = it.body?.stroke_probability ?: 0F,
                    isHighWeight = bmi > 25,
                    isLowWeight = bmi < 18.5,
                    isAge = age > 50,
                    isBloodPressure = userHealthPostReq.highBloodPressure,
                    isHeartDisease = userHealthPostReq.heartDisease
                )))
        }.exceptionally { ex ->
            println("Exception occurred: ${ex.message}")
            ex.printStackTrace()
        }
        return ResponseEntity.ok().build()
    }

    //본인이 작성한 건강정보들을 조회하는 메서드
    fun getList(pageNumber: Int, pageSize: Int, userId: Long): ResponseEntity<List<UserHealthGetElementRes>> {
        return ResponseEntity.ok(userHealthRepository.findByUserOrderByIdDesc(User(userId), PageRequest.of(pageNumber, pageSize)).map{
                userHealth -> UserHealthGetElementRes(userHealth)
        })
    }

    fun get(id: Int?, userId: Long): ResponseEntity<UserHealthGetRes> {
        return ResponseEntity.ok(
            UserHealthGetRes(if(id == null)
                userHealthRepository.findTopByUserOrderByIdDesc(User(userId))
            else
                userHealthRepository.findById(id).orElseThrow { NullPointerException() } ?: throw NullPointerException())
        )

    }

    fun delete(id: Int, userId: Long): ResponseEntity<HttpStatus> {
        CompletableFuture.runAsync {
            println(if(userHealthRepository.deleteByIdAndUser(id, User(userId)) == 1) "DELETE COMPLETE: id=$id"
            else "DELETE INCOMPLETE: id=$id")
        }
        return ResponseEntity.ok().build()
    }
}