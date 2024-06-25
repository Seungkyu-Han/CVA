package knu.cpa.application.impl

import knu.cpa.application.AuthApplication
import knu.cpa.config.jwt.JwtTokenProvider
import knu.cpa.model.dto.auth.req.AuthPostReq
import knu.cpa.model.dto.auth.res.AuthGetRes
import knu.cpa.model.dto.auth.res.AuthKakaoAccessRes
import knu.cpa.model.dto.auth.res.AuthKakaoInfoRes
import knu.cpa.model.dto.auth.res.AuthLoginRes
import knu.cpa.model.entity.User
import knu.cpa.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class AuthApplicationImpl(
    @Value("\${kakao.auth.client_id}")
    private val clientId: String,
    @Value("\${kakao.auth.redirect_uri}")
    private val redirectUri: String,
    @Value("\${kakao.auth.local_redirect_uri}")
    private val localRedirectUri: String,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
): AuthApplication{

    private val accessTokenReqUrl = "https://kauth.kakao.com/oauth/token"
    private val jsonReqUrl = "https://kapi.kakao.com/v2/user/me"
    override fun getLocalLogin(code: String): ResponseEntity<AuthLoginRes> {

        val kakaoAccessToken = getLocalKakaoAccessToken(code) ?: throw NullPointerException()

        val authKakaoInfoRes = getIdByKakao(kakaoAccessToken) ?: throw NullPointerException()

        val optionalUser = userRepository.findById(authKakaoInfoRes.id)

        if (optionalUser.isEmpty)
            return register(authKakaoInfoRes)

        val accessToken = jwtTokenProvider.createAccessToken(authKakaoInfoRes.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(authKakaoInfoRes.id)

        val user = optionalUser.get()
        userRepository.save(user)

        return ResponseEntity.ok(AuthLoginRes(refreshToken = refreshToken, accessToken = accessToken))
    }

    //로그인 토큰을 받아오는 메서드
    override fun getLogin(code: String): ResponseEntity<AuthLoginRes> {

        val kakaoAccessToken = getKakaoAccessToken(code) ?: throw NullPointerException()

        val authKakaoInfoRes = getIdByKakao(kakaoAccessToken) ?: throw NullPointerException()

        val optionalUser = userRepository.findById(authKakaoInfoRes.id)

        if (optionalUser.isEmpty)
            return register(authKakaoInfoRes)

        val accessToken = jwtTokenProvider.createAccessToken(authKakaoInfoRes.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(authKakaoInfoRes.id)

        val user = optionalUser.get()
        userRepository.save(user)

        return ResponseEntity.ok(AuthLoginRes(refreshToken = refreshToken, accessToken = accessToken))
    }

    override fun patchLogin(refreshToken: String): ResponseEntity<AuthLoginRes> {
        val token = refreshToken.split(" ")[1]
        val userId = jwtTokenProvider.getId(token)

        val accessToken = jwtTokenProvider.createAccessToken(userId ?: throw NullPointerException())

        return ResponseEntity.ok(AuthLoginRes(refreshToken = token, accessToken = accessToken))
    }

    override fun postInfo(authPostReq: AuthPostReq, authentication: Authentication): ResponseEntity<HttpStatus> {
        val user = User(authentication)

        user.name = authPostReq.name
        user.birthday = authPostReq.birthday
        user.gender = authPostReq.gender

        userRepository.save(user)

        return ResponseEntity(HttpStatus.OK)
    }

    override fun getInfo(authentication: Authentication): ResponseEntity<AuthGetRes> {
        return ResponseEntity(
            AuthGetRes(userRepository.findById(authentication.name.toLong()).orElseThrow()), HttpStatus.OK
        )
    }
    
    private fun register(authKakaoInfoRes: AuthKakaoInfoRes): ResponseEntity<AuthLoginRes>{

        val accessToken = jwtTokenProvider.createAccessToken(authKakaoInfoRes.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(authKakaoInfoRes.id)

        val user = User(id = authKakaoInfoRes.id, name = authKakaoInfoRes.properties.nickname, profileImg = authKakaoInfoRes.kakaoAccount.profile.profileImageUrl, birthday = null, gender = null)
        userRepository.save(user)

        return ResponseEntity.ok(AuthLoginRes(refreshToken = refreshToken, accessToken = accessToken))
    }

    private fun getIdByKakao(token: String): AuthKakaoInfoRes? {

        val restTemplate = RestTemplate()

        val httpHeaders = HttpHeaders()
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        httpHeaders.add("Authorization", "Bearer $token")

        return restTemplate.exchange(
            RequestEntity<Any>(httpHeaders, HttpMethod.POST, URI.create(jsonReqUrl)),
            AuthKakaoInfoRes::class.java
        ).body
    }

    private fun getKakaoAccessToken(code: String):String?{

        val restTemplate = RestTemplate()

        val httpHeaders = HttpHeaders()

        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")

        val httpBody = LinkedMultiValueMap<String, String>()

        httpBody.add("grant_type","authorization_code")
        httpBody.add("client_id", clientId)
        httpBody.add("redirect_uri", redirectUri)
        httpBody.add("code",code)

        return restTemplate.postForObject(accessTokenReqUrl, HttpEntity(httpBody, httpHeaders), AuthKakaoAccessRes::class.java)?.access_token
    }

    private fun getLocalKakaoAccessToken(code: String): String?{

        val restTemplate = RestTemplate()

        val httpHeaders = HttpHeaders()

        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")

        val httpBody = LinkedMultiValueMap<String, String>()

        httpBody.add("grant_type","authorization_code")
        httpBody.add("client_id", clientId)
        httpBody.add("redirect_uri", localRedirectUri)
        httpBody.add("code",code)

        return restTemplate.postForObject(accessTokenReqUrl, HttpEntity(httpBody, httpHeaders), AuthKakaoAccessRes::class.java)?.access_token
    }
}