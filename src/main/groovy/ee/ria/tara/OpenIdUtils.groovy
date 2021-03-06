package ee.ria.tara

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.*
import com.nimbusds.jwt.SignedJWT
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang.RandomStringUtils
import io.restassured.response.Response

class OpenIdUtils {
    static Boolean isTokenSignatureValid(JWKSet jwkSet, SignedJWT signedJWT) throws JOSEException {
        List<JWK> matches = new JWKSelector(new JWKMatcher.Builder()
                .keyType(KeyType.RSA)
                .build())
                .select(jwkSet)

        RSAKey rsaKey = (RSAKey) matches.get(0)
        JWSVerifier verifier = new RSASSAVerifier(rsaKey)
        return signedJWT.verify(verifier)
    }

    static Map<String, String> getAuthorizationParameters(Flow flow, String scope = "openid", String uiLocales = "et") {
        Map<String, String> queryParams = new HashMap<>()
        flow.setState(Base64.getEncoder().encodeToString(DigestUtils.sha256(RandomStringUtils.random(16))))
        flow.setNonce(Base64.getEncoder().encodeToString(DigestUtils.sha256(RandomStringUtils.random(16))))
        queryParams.put("ui_locales", uiLocales)
        queryParams.put("response_type", "code")
        queryParams.put("scope", scope)
        queryParams.put("client_id",flow.getOidcClient().getClientId())
        queryParams.put("redirect_uri", flow.getOidcClient().getFullResponseUrl().toString())
        queryParams.put("state", flow.state)
        queryParams.put("nonce", flow.nonce)
        return queryParams
    }

    static Map<String, String> getAuthorizationParametersForSpecificProxyService(Flow flow, String scope = "openid", String uiLocales = "et") {
        Map<String, String> queryParams = new HashMap<>()
        flow.setState(Base64.getEncoder().encodeToString(DigestUtils.sha256(RandomStringUtils.random(16))))
        flow.setNonce(Base64.getEncoder().encodeToString(DigestUtils.sha256(RandomStringUtils.random(16))))
        queryParams.put("ui_locales", uiLocales)
        queryParams.put("response_type", "code")
        queryParams.put("scope", scope)
        queryParams.put("client_id",flow.getSpecificProxyService().getClientId())
        queryParams.put("redirect_uri", flow.getSpecificProxyService().getFullResponseUrl().toString())
        queryParams.put("state", flow.state)
        queryParams.put("nonce", flow.nonce)
        return queryParams
    }

    static Map<String, String> getErrorFromAuthorizationHeader(Response errorResponse) {
        String[] errorList = errorResponse.getHeader("WWW-Authenticate").toString().split(",")
        Map<String, String> myMap = new HashMap<String, String>()
        for (String str : errorList) {
            myMap.put(str.split("=")[0], str.split("=")[1])
        }
        return myMap
    }
}
