package api;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.UserStellar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.UserClient;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static testValue.TestValue.*;

/**
 2. Логин пользователя:
 * логин под существующим пользователем,
 * логин с неверным логином и паролем.
 */
public class LoginUserTest {
    private UserClient userClient;
    private ValidatableResponse responseLogin;
    private UserStellar userStellar;


    @Before
    @Step("Предусловие.Создание пользователя")
    public void setUp() {
        userClient = new UserClient();
        userStellar = new UserStellar(TEST_LOGIN_ONE, TEST_PASSWORD_ONE, TEST_NAME_ONE);
        responseLogin = userClient.createUser(userStellar);
    }

    @Test
    @DisplayName("Логин под существующим пользователем. Ответ 200 / Проверка body")
    @Description("Post запрос на ручку /api/auth/login")
    @Step("Основной шаг - логин пользователя")
    public void loginWithUserTrueAndCheckBody() {
        responseLogin.assertThat().statusCode(HTTP_OK);
        responseLogin.assertThat().body("success", equalTo(true));
        responseLogin.assertThat().body("accessToken", startsWith("Bearer "))
                .and()
                .body("refreshToken", notNullValue());
        responseLogin.assertThat().body("user.email", equalTo(TEST_LOGIN_ONE))
                .and()
                .body("user.name", equalTo(TEST_NAME_ONE));
    }

    @Test
    @DisplayName("Логин под неверным именем почты. Ответ 401 / Проверка body")
    @Description("Post запрос на ручку /api/auth/login")
    @Step("Основной шаг - логин пользователя")
    public void loginWithUserFalseAndCheckBody() {
        userStellar.setEmail(TEST_LOGIN_TWO);
        userClient.loginUser(userStellar).assertThat().statusCode(HTTP_UNAUTHORIZED);
        userClient.loginUser(userStellar)
                .assertThat().body("success", equalTo(false))
                .and()
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин под неверным паролем. Ответ 401 / Проверка body")
    @Description("Post запрос на ручку /api/auth/login")
    @Step("Основной шаг - логин пользователя")
    public void loginWithUserFalsePasswordAndCheckBody() {
        userStellar.setPassword(TEST_PASSWORD_TWO);
        userClient.loginUser(userStellar)
                .assertThat().statusCode(HTTP_UNAUTHORIZED);
        userClient.loginUser(userStellar)
                .assertThat().body("success", equalTo(false))
                .and()
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    @Step("Постусловие.Удаление пользователя")
    public void clearData() {
        try {
            String accessTokenWithBearer = responseLogin.extract().path("accessToken");
            String accessToken = accessTokenWithBearer.replace("Bearer ", "");
            userClient.deleteUser(accessToken);
            System.out.println("удален");
        } catch (Exception e) {
            System.out.println("Пользователь не удалился. Возможно ошибка при создании");
        }
    }
}