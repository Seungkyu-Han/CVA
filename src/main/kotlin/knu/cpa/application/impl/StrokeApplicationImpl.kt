package knu.cpa.application.impl

import knu.cpa.application.StrokeApplication
import knu.cpa.model.dto.stroke.res.StrokeGetElementRes
import knu.cpa.model.entity.User
import knu.cpa.repository.StrokeRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class StrokeApplicationImpl(private val strokeRepository: StrokeRepository): StrokeApplication {

    //저장되어 있는 Stroke 확률 리스트를 조회하는 메서드
    override fun getList(authentication: Authentication): ResponseEntity<List<StrokeGetElementRes>> {
        return ResponseEntity.ok(
        strokeRepository.findByUser(User(authentication)).map {
            StrokeGetElementRes(it)
        })
    }

    //저장되어 있는 Stroke 확률을 상세하게 조회하는 메서드

    override fun get(strokeId: Int, authentication: Authentication): ResponseEntity<StrokeGetElementRes> {
        return ResponseEntity.ok(
            StrokeGetElementRes(strokeRepository.findById(strokeId).orElseThrow())
        )
    }
}