package knu.cpa.application

import knu.cpa.model.dto.auth.req.AuthPostReq
import knu.cpa.model.dto.auth.res.AuthGetRes
import knu.cpa.model.dto.auth.res.AuthLoginRes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication

//ServiceImpl의 설명 참조
interface AuthApplication {
    fun getLogin(code: String): ResponseEntity<AuthLoginRes>
    fun patchLogin(refreshToken: String): ResponseEntity<AuthLoginRes>
    fun postInfo(authPostReq: AuthPostReq, authentication: Authentication): ResponseEntity<HttpStatus>
    fun getInfo(authentication: Authentication): ResponseEntity<AuthGetRes>
    fun getLocalLogin(code: String): ResponseEntity<AuthLoginRes>
}