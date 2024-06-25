package knu.cpa.application.authImpl

import com.fasterxml.jackson.databind.ObjectMapper
import knu.cpa.application.UserHealthApplication
import knu.cpa.application.impl.UserHealthApplicationImpl
import knu.cpa.model.dto.userHealth.req.UserHealthPostReq
import knu.cpa.model.dto.userHealth.res.UserHealthGetElementRes
import knu.cpa.model.dto.userHealth.res.UserHealthGetRes
import knu.cpa.repository.StrokeRepository
import knu.cpa.repository.UserHealthRepository
import knu.cpa.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class UserHealthAuthApplicationImpl(
    private val userHealthRepository: UserHealthRepository,
    private val strokeRepository: StrokeRepository,
    private val userRepository: UserRepository,
    @Value("\${fastApi}")
    private val fastApiServer: String,
    private val objectMapper: ObjectMapper
): UserHealthApplication, UserHealthApplicationImpl(
    userHealthRepository, strokeRepository, userRepository, fastApiServer, objectMapper
)
{
    override fun post(
        userHealthPostReq: UserHealthPostReq,
        authentication: Authentication
    ): ResponseEntity<HttpStatus> {
        return this.post(userHealthPostReq, authentication.name.toLong())
    }

    override fun getList(
        pageNumber: Int,
        pageSize: Int,
        authentication: Authentication
    ): ResponseEntity<List<UserHealthGetElementRes>> {
        return this.getList(pageNumber, pageSize, authentication.name.toLong())
    }

    override fun get(id: Int?, authentication: Authentication): ResponseEntity<UserHealthGetRes> {
        return this.get(id, authentication.name.toLong())
    }

    override fun delete(id: Int, authentication: Authentication): ResponseEntity<HttpStatus> {
        return this.delete(id, authentication.name.toLong())
    }
}