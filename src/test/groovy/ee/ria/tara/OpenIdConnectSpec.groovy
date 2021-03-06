package ee.ria.tara

import com.nimbusds.jwt.JWTClaimsSet
import io.qameta.allure.Feature
import io.restassured.filter.cookie.CookieFilter
import io.restassured.response.Response
import org.hamcrest.Matchers
import spock.lang.Ignore;
import spock.lang.Unroll
import com.nimbusds.jose.jwk.JWKSet

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith
import static org.junit.jupiter.api.Assertions.*
import static org.hamcrest.MatcherAssert.assertThat

class OpenIdConnectSpec extends TaraSpecification {
    Flow flow = new Flow(props)

    def setup() {
        flow.cookieFilter = new CookieFilter()
        flow.openIdServiceConfiguration = Requests.getOpenidConfiguration(flow.oidcService.fullConfigurationUrl)
        flow.jwkSet = JWKSet.load(Requests.getOpenidJwks(flow.oidcService.fullJwksUrl))
    }

    @Unroll
    @Feature("OPENID_CONNECT")
    def "Metadata and token key ID matches"() {
        expect:
        Steps.startAuthenticationInTara(flow)
        Response midAuthResponse = Steps.authenticateWithMid(flow,"60001017727" , "69200366")
        Response authenticationFinishedResponse = Steps.submitConsentAndFollowRedirects(flow, true, midAuthResponse)
        Response tokenResponse = Steps.getIdentityTokenResponse(flow, authenticationFinishedResponse)
        assertEquals(200, tokenResponse.statusCode(), "Correct HTTP status code is returned")
        String keyID = Steps.verifyTokenAndReturnSignedJwtObject(flow, tokenResponse.getBody().jsonPath().get("id_token")).getHeader().getKeyID()
        assertThat(keyID, equalTo(flow.jwkSet.getKeys().get(0).getKeyID()))
    }

    @Unroll
    @Feature("OPENID_CONNECT")
    def "Request a token twice"() {
        expect:
        Response initOIDCServiceSession = Steps.startAuthenticationInOidc(flow)
        assertEquals(302, initOIDCServiceSession.statusCode(), "Correct HTTP status code is returned")
        Response initLoginSession = Steps.createLoginSession(flow, initOIDCServiceSession)
        assertEquals(200, initLoginSession.statusCode(), "Correct HTTP status code is returned")
        Response midAuthResponse = Steps.authenticateWithMid(flow,"60001017727" , "69200366")
        Response authenticationFinishedResponse = Steps.submitConsentAndFollowRedirects(flow, true, midAuthResponse)
        String authorizationCode = Utils.getParamValueFromResponseHeader(authenticationFinishedResponse, "code")
        // 1
        Requests.getWebToken(flow, authorizationCode)
        // 2
        Response tokenResponse2 = Requests.getWebToken(flow, authorizationCode)
        assertEquals(400, tokenResponse2.statusCode(), "Correct HTTP status code is returned")
        assertThat("Correct Content-Type is returned", tokenResponse2.getContentType(), startsWith("application/json"))
        assertEquals("invalid_grant", tokenResponse2.body().jsonPath().get("error"), "Correct error message is returned")
        assertThat("Correct error_description is returned", tokenResponse2.body().jsonPath().getString("error_description"), Matchers.endsWith("The authorization code has already been used."))
    }

    @Ignore // Etapp 4
    @Unroll
    @Feature("OPENID_CONNECT")
    def "Request with empty scope"() {
        expect:
        Map<String, String> paramsMap = OpenIdUtils.getAuthorizationParameters(flow)
        paramsMap.put("scope", "")
        Response response = Steps.startAuthenticationInOidcWithParams(flow, paramsMap)
        assertEquals(302, response.statusCode(), "Correct HTTP status code is returned")
        assertEquals(" error text here", Utils.getParamValueFromResponseHeader(response, "error"), "Correct error value")
    }

    @Unroll
    @Feature("OPENID_CONNECT")
    def "Request with invalid authorization code"() {
        expect:
        Response initOIDCServiceSession = Steps.startAuthenticationInOidc(flow)
        assertEquals(302, initOIDCServiceSession.statusCode(), "Correct HTTP status code is returned")
        Response initLoginSession = Steps.createLoginSession(flow, initOIDCServiceSession)
        assertEquals(200, initLoginSession.statusCode(), "Correct HTTP status code is returned")
        Response midAuthResponse = Steps.authenticateWithMid(flow,"60001017727" , "69200366")
        Response authenticationFinishedResponse = Steps.submitConsentAndFollowRedirects(flow, true, midAuthResponse)
        String authorizationCode = Utils.getParamValueFromResponseHeader(authenticationFinishedResponse, "code")

        Response response = Requests.getWebToken(flow, authorizationCode + "e")
        assertEquals(400, response.statusCode(), "Correct HTTP status code is returned")
        assertThat("Correct Content-Type is returned", response.getContentType(), startsWith("application/json"))
        assertEquals("invalid_grant", response.body().jsonPath().get("error"), "Correct error message is returned")
    }

    @Unroll
    @Feature("OPENID_CONNECT")
    def "Request with missing parameter #paramName"() {
        expect:
        HashMap<String, String> formParamsMap = (HashMap) Collections.emptyMap()
        def map1 = Utils.setParameter(formParamsMap, "grant_type", "code")
        def map2 = Utils.setParameter(formParamsMap, "code", "1234567")
        def map3 = Utils.setParameter(formParamsMap, "redirect_uri", flow.oidcClient.fullResponseUrl)
        formParamsMap.remove(paramName)
        Response response = Requests.getWebTokenResponseBody(flow, formParamsMap)
        assertEquals(statusCode, response.statusCode(), "Correct HTTP status code is returned")
        assertThat("Correct Content-Type is returned", response.getContentType(), startsWith("application/json"))
        assertEquals(error, response.body().jsonPath().get("error"), "Correct error message is returned")
        String errorDescription = response.body().jsonPath().get("error_description")
        assertThat("Correct error_description suffix", errorDescription, startsWith(errorSuffix))
        assertThat("Correct error_description preffix", errorDescription, Matchers.endsWith(errorPreffix))

        where:
        paramName      || statusCode || error             || errorSuffix || errorPreffix
        "code"         || 400        || "invalid_request" || "The request is missing a required parameter" || "whitelisted the redirect_uri you specified."
        "grant_type"   || 400        || "invalid_request" || "The request is missing a required parameter" || "Request parameter 'grant_type' is missing"
        "redirect_uri" || 400        || "invalid_request" || "The request is missing a required parameter" || "whitelisted the redirect_uri you specified."
    }


    @Unroll
    @Feature("OPENID_CONNECT")
    def "Request with invalid parameter value #paramName"() {
        expect:
        HashMap<String, String> formParamsMap = (HashMap) Collections.emptyMap()
        def map1 = Utils.setParameter(formParamsMap, "grant_type", "code")
        def map2 = Utils.setParameter(formParamsMap, "code", "1234567")
        def map3 = Utils.setParameter(formParamsMap, "redirect_uri", flow.oidcClient.fullResponseUrl)
        def map4 = Utils.setParameter(formParamsMap, paramName, paramValue)
        Response response = Requests.getWebTokenResponseBody(flow, formParamsMap)
        assertEquals(statusCode, response.statusCode(), "Correct HTTP status code is returned")
        assertThat("Correct Content-Type is returned", response.getContentType(), startsWith("application/json"))
        assertEquals(error, response.body().jsonPath().get("error"), "Correct error message is returned")
        String errorDescription = response.body().jsonPath().get("error_description")
        assertThat("Correct error_description suffix", errorDescription, startsWith(errorSuffix))
        assertThat("Correct error_description preffix", errorDescription, Matchers.endsWith(errorPreffix))

        where:
        paramName      | paramValue                || statusCode || error             || errorSuffix || errorPreffix
        "redirect_uri" | "https://www.example.com" || 400        || "invalid_request" || "The request is missing a required parameter" || "whitelisted the redirect_uri you specified."
        "grant_type"   | "token"                   || 400        || "invalid_request" || "The request is missing a required parameter" || "whitelisted the redirect_uri you specified."
        "code"         | "45678"                   || 400        || "invalid_request" || "The request is missing a required parameter" || "whitelisted the redirect_uri you specified."
    }

    @Unroll
    @Feature("OPENID_CONNECT")
    def "Request with url encoded state and nonce"() {
        expect:
        Map<String, String> paramsMap = OpenIdUtils.getAuthorizationParameters(flow)
        flow.setState("test?????\uD83D\uDE0D&additional=1 %20")
        flow.setNonce("test?????\uD83D\uDE0D&additional=1 %20")
        paramsMap.put("state", "test?????\uD83D\uDE0D&additional=1 %20")
        paramsMap.put("nonce", "test?????\uD83D\uDE0D&additional=1 %20")
        Response initOIDCServiceSession = Steps.startAuthenticationInOidcWithParams(flow, paramsMap)
        Response initLoginSession = Steps.createLoginSession(flow, initOIDCServiceSession)
        assertEquals(200, initLoginSession.statusCode(), "Correct HTTP status code is returned")
        Response midAuthResponse = Steps.authenticateWithMid(flow,"60001017716", "69100366")
        Response authenticationFinishedResponse = Steps.submitConsentAndFollowRedirects(flow, true, midAuthResponse)
        Response tokenResponse = Steps.getIdentityTokenResponse(flow, authenticationFinishedResponse)

        JWTClaimsSet claims = Steps.verifyTokenAndReturnSignedJwtObject(flow, tokenResponse.getBody().jsonPath().get("id_token")).getJWTClaimsSet()
        assertThat(claims.getClaim("nonce"), equalTo(paramsMap.get("nonce")))
        assertThat(claims.getClaim("state"), equalTo(paramsMap.get("state")))
    }

}
