package ee.ria.tara

import io.qameta.allure.Step
import io.qameta.allure.restassured.AllureRestAssured
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.response.Response

import static io.restassured.RestAssured.given
import static io.restassured.config.EncoderConfig.encoderConfig

class Requests {

    @Step("Mobile-ID authentication init request")
    static Response startMidAuthentication(Flow flow, String idCode, String phoneNo) {
        Response response =
                given()
                        .filter(flow.cookieFilter)
                        .filter(new AllureRestAssured())
                        .formParam("idCode", idCode)
                        .formParam("telephoneNumber", phoneNo)
                        .cookie("SESSION", flow.sessionId)
                        .formParam("_csrf", flow.csrf)
                        .log().cookies()
                        .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                        .when()
                        .redirects().follow(false)
                        .post(flow.loginService.fullMidInitUrl)
                        .then()
                        .extract().response()
        return response
    }

    @Step("Mobile-ID response poll request")
    static Response pollMid(Flow flow) {
        Response response =
                given()
                        .filter(flow.cookieFilter)
                        .filter(new AllureRestAssured())
                        .cookie("SESSION", flow.sessionId)
                        .log().cookies()
                        .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                        .when()
                        .redirects().follow(false)
                        .get(flow.loginService.fullMidPollUrl)
                        .then()
                        .extract().response()
        return response
    }

    @Step("Smart-ID response poll request")
    static Response pollSid(Flow flow) {
        Response response =
                given()
                        .filter(flow.cookieFilter)
                        .filter(new AllureRestAssured())
                        .cookie("SESSION", flow.sessionId)
                        .cookie("LOGIN_LOCALE", flow.login_locale)
                        .log().cookies()
                        .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                        .when()
                        .redirects().follow(false)
                        .get(flow.loginService.fullSidPollUrl)
                        .then()
                        .extract().response()
        return response
    }

    @Step("Follow redirect request")
    static Response followRedirect(Flow flow, String location) {
        return given()
                .filter(new AllureRestAssured())
                .relaxedHTTPSValidation()
                .log().cookies()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(true)
                .get(location)
                .then()
                .extract().response()
    }

    @Step("Simple get request")
    static Response getRequest(String location) {
        return given()
                .filter(new AllureRestAssured())
                .relaxedHTTPSValidation()
                .log().cookies()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(true)
                .get(location)
                .then()
                .extract().response()
    }

    @Step("Login service get request with session id")
    static Response getRequestWithSessionId(Flow flow, String location) {
        return given()
                .filter(flow.cookieFilter)
                .filter(new AllureRestAssured())
                .cookie("SESSION", flow.sessionId)
                .log().cookies()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .baseUri(flow.loginService.baseUrl)
                .when()
                .redirects().follow(false)
                .get(location)
                .then()
                .extract().response()
    }

    @Step("Login service post request with session id")
    static Response postRequestWithSessionId(Flow flow, String location) {
        return given()
                .filter(flow.cookieFilter)
                .filter(new AllureRestAssured())
                .cookie("SESSION", flow.sessionId)
                .formParam("_csrf", flow.csrf)
                .log().cookies()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .baseUri(flow.loginService.baseUrl)
                .when()
                .redirects().follow(false)
                .post(location)
                .then()
                .extract().response()
    }

    @Step("Follow redirect request with session id and cookies")
    static Response followRedirectWithSessionIdAndCookies(Flow flow, String location, Map cookies) {
        return given()
                .filter(flow.cookieFilter)
                .filter(new AllureRestAssured())
                .cookie("SESSION", flow.sessionId)
                .cookies(cookies)
                .log().cookies()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .baseUri(flow.loginService.baseUrl)
                .when()
                .redirects().follow(false)
                .get(location)
                .then()
                .extract().response()
    }

    @Step("Follow redirect with cookie request")
    static Response followRedirectWithCookie(Flow flow, String location, Map myCookies) {
        return given()
                .filter(flow.cookieFilter)
                .cookies(myCookies)
                .filter(new AllureRestAssured())
                .log().cookies()
                .relaxedHTTPSValidation()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .get(location)
                .then()
                .extract().response()
    }

    @Step("Get request with cookies and params")
    static Response getRequestWithCookiesAndParams(Flow flow , String url
                                                   , Map<String, String> cookies
                                                   , Map<String, String> queryParams
                                                   , Map<String, String> additionalQueryParams) {
        return given()
                .filter(flow.cookieFilter)
                .cookies(cookies)
                .queryParams(queryParams)
                .queryParams(additionalQueryParams)
                .filter(new AllureRestAssured())
                .log().cookies()
                .relaxedHTTPSValidation()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .get(url)
                .then()
                .extract().response()
    }

    @Step("Get request with sessionID, cookies and params")
    static Response getRequestWithSessionIDCookiesAndParams(Flow flow , String url
                                                   , Map<String, String> cookies
                                                   , Map<String, String> queryParams
                                                   , Map<String, String> additionalQueryParams) {
        return given()
                .filter(flow.cookieFilter)
                .cookie("SESSION", flow.sessionId)
                .cookies(cookies)
                .queryParams(queryParams)
                .queryParams(additionalQueryParams)
                .filter(new AllureRestAssured())
                .log().cookies()
                .relaxedHTTPSValidation()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .get(url)
                .then()
                .extract().response()
    }

    @Step("Get request with params")
    static Response getRequestWithParams(Flow flow , String url
                                                   , Map<String, String> queryParams
                                                   , Map<String, String> additionalQueryParams) {
        return given()
                .filter(flow.cookieFilter)
                .queryParams(queryParams)
                .queryParams(additionalQueryParams)
                .filter(new AllureRestAssured())
                .log().cookies()
                .relaxedHTTPSValidation()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(true)
                .get(url)
                .then()
                .extract().response()
    }

    @Step("Get request with headers and params")
    static Response getRequestWithHeadersAndParams(Flow flow , String url
                                           ,Map<String, String> headers
                                           , Map<String, String> queryParams
                                           , Map<String, String> additionalQueryParams) {
        return given()
                .filter(flow.cookieFilter)
                .queryParams(queryParams)
                .headers(headers)
                .queryParams(additionalQueryParams)
                .filter(new AllureRestAssured())
                .log().cookies()
                .relaxedHTTPSValidation()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(true)
                .get(url)
                .then()
                .extract().response()
    }
    @Step("Post request with cookies and params")
    static Response postRequestWithCookiesAndParams(Flow flow , String url
                                                   , Map<String, String> cookies
                                                   , Map<String, String> formParams
                                                   , Map<String, String> additionalFormParams) {
        return given()
                .filter(flow.cookieFilter)
                .filter(new AllureRestAssured())
                .cookies(cookies)
                .formParams(formParams)
                .formParams(additionalFormParams)
                .log().cookies()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .when()
                .post(url)
                .then()
                .extract().response()
    }

    @Step("Post request with params")
    static Response postRequestWithParams(Flow flow , String url
                                                    , Map<String, String> formParams
                                                    , Map<String, String> additionalFormParams) {
        return given()
                .filter(flow.cookieFilter)
                .filter(new AllureRestAssured())
                .formParams(formParams)
                .formParams(additionalFormParams)
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .baseUri(flow.loginService.baseUrl)
                .log().cookies()
                .when()
                .post(url)
                .then()
                .extract().response()
    }

    @Step("Post request with headers, cookies and params")
    static Response postRequestWithHeadersCookiesAndParams(Flow flow , String url
                                                    , Map<String, String> headers
                                                    , Map<String, String> cookies
                                                    , Map<String, String> formParams) {
        return given()
                .filter(flow.cookieFilter)
                .filter(new AllureRestAssured())
                .cookies(cookies)
                .headers(headers)
                .formParams(formParams)
                .log().cookies()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .when()
                .post(url)
                .then()
                .extract().response()
    }

    @Step("Get heartbeat")
    static Response getHeartbeat(Flow flow) {
        return given()
                .filter(new AllureRestAssured())
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .when()
                .get(flow.loginService.fullHeartbeatUrl)
                .then()
                .statusCode(200)
                .extract().response()
    }

    @Step("Get request with ID-Card authentication")
    static Response idCardAuthentication(Flow flow , Map<String, String> headers) {
        return given()
                .filter(flow.cookieFilter)
                .headers(headers)
                .auth().preemptive().basic(flow.loginService.idCardEndpointUsername, flow.loginService.idCardEndpointPassword)
                .cookie("SESSION", flow.sessionId)
                .relaxedHTTPSValidation()
                .log().cookies()
                .filter(new AllureRestAssured())
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .get(flow.loginService.fullIdCardInitUrl)
                .then()
                .extract().response()
    }

    @Step("Get request with ID-Card authentication and without session cookie")
    static Response idCardAuthenticationWithoutSession(Flow flow , Map<String, String> headers) {
        return given()
                .filter(flow.cookieFilter)
                .headers(headers)
                .auth().preemptive().basic(flow.loginService.idCardEndpointUsername, flow.loginService.idCardEndpointPassword)
                .relaxedHTTPSValidation()
                .log().cookies()
                .filter(new AllureRestAssured())
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .get(flow.loginService.fullIdCardInitUrl)
                .then()
                .extract().response()
    }

    @Step("Download openid service configuration")
    static JsonPath getOpenidConfiguration(String url) {
        return given()
                .filter(new AllureRestAssured())
                .relaxedHTTPSValidation()
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .extract().body().jsonPath()
    }

    @Step("Download openid service JWKS")
    static InputStream getOpenidJwks(String url) {
        return given()
                .filter(new AllureRestAssured())
                .relaxedHTTPSValidation()
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .extract().body().asInputStream()
    }

    @Step("Get token")
    static Response getWebToken(Flow flow, String authorizationCode) {
        return given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .formParam("grant_type", "authorization_code")
                .formParam("code", authorizationCode)
                .formParam("redirect_uri", flow.oidcClient.fullResponseUrl)
                .auth().preemptive().basic(flow.oidcClient.clientId, flow.oidcClient.clientSecret)
                .when()
                .urlEncodingEnabled(true)
                .post(flow.openIdServiceConfiguration.getString("token_endpoint"))
                .then()
                .extract().response()
    }

    @Step("Get token with client")
    static Response getWebTokenWithClient(Flow flow, String authorizationCode, String redirectUri, String clientId, String clientSecret) {
        return given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .formParam("grant_type", "authorization_code")
                .formParam("code", authorizationCode)
                .formParam("redirect_uri", redirectUri)
                .auth().preemptive().basic(clientId, clientSecret)
                .when()
                .urlEncodingEnabled(true)
                .post(flow.openIdServiceConfiguration.getString("token_endpoint"))
                .then()
                .extract().response()
    }

    @Step("Get token response body")
    static Response getWebTokenResponseBody(Flow flow, Map<String, String> formParams) {
        return given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .formParams(formParams)
                .auth().preemptive().basic(flow.oidcClient.clientId, flow.oidcClient.clientSecret)
                .when()
                .urlEncodingEnabled(true)
                .post(flow.openIdServiceConfiguration.getString("token_endpoint"))
                .then()
                .extract().response()
    }

    @Step("Get user info with header parameter")
    static Response getUserInfoWithHeaderParam(Flow flow, String requestType, Map<String, String> headers) {
        return given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .headers(headers)
                .when()
                .urlEncodingEnabled(true)
                .request(requestType, flow.openIdServiceConfiguration.getString("userinfo_endpoint"))
                .then()
                .extract().response()
    }

    @Step("Get user info with query parameter")
    static Response getUserInfoWithQueryParam(Flow flow, String requestType, Map<String, String> queryParams) {
        return given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .params(queryParams)
                .when()
                .urlEncodingEnabled(true)
                .request(requestType, flow.openIdServiceConfiguration.getString("userinfo_endpoint"))
                .then()
                .extract().response()
    }

    @Step("Post request with json body for admin api")
    static Response jsonRequest(Flow flow, String location, Map<String, Object> jsonAsMap) {
        return given()
                .filter(flow.cookieFilter)
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(jsonAsMap)
                .when()
                .urlEncodingEnabled(true)
                .post(location)
                .then()
                .log().cookies()
                .extract().response()
    }

    @Step("Post request with json for admin api")
    static Response postRequestWithJsonBody(Flow flow, String location, Map<String, String> cookies, String body) {
        return given()
                .filter(flow.cookieFilter)
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .header("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN"))
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .urlEncodingEnabled(true)
                .post(location)
                .then()
                .log().cookies()
                .extract().response()
    }

    @Step("Delete request for admin api")
    static Response deleteRequest(Flow flow, String location, Map<String, String> cookies) {
        return given()
                .filter(flow.cookieFilter)
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .header("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN"))
                .cookies(cookies)
                .when()
                .urlEncodingEnabled(true)
                .delete(location)
                .then()
                .extract().response()
    }

    @Step("Get request for admin api")
    static Response getRequest(Flow flow, String location, Map<String, String> cookies) {
        return given()
                .filter(flow.cookieFilter)
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"))).relaxedHTTPSValidation()
                .filter(new AllureRestAssured())
                .header("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN"))
                .cookies(cookies)
                .when()
                .urlEncodingEnabled(true)
                .get(location)
                .then()
                .extract().response()
    }
}
