package org.example.design;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.comparison.ImageMarkupPolicy;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.cropper.indent.IndentCropper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import io.qameta.allure.*;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

@Epic("Дизайн")
@Feature("Проверка визуального дизайна")
public class DesignTest {

    @BeforeAll
    public static void setUp() {
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
    }

    @Test
    @Story("Проверка дизайна на разрешении 1920px")
    @DisplayName("Проверка дизайна на разрешении 1920px")
    public void testDesignOn1920px() throws IOException {
        // Открываем веб-страницу
        open("https://pgu-uat-fed.test.gosuslugi.ru/600103/1/form");
        // Вводим логин
        $("#login").setValue("551-703-571 75");
        $("#password").setValue("ckPekf}WPfm~");
        $(By.xpath("/html/body/esia-root/div/esia-login/div/div[1]/form/div[4]/button"))
                .click();
        // Если есть черновик - раскомментить чтобы продолжить
//        $(By.xpath("//*[@id='print-page']/portal-new-sf-player/epgu-cf-ui-modal-container/div/" +
//                "epgu-constructor-confirmation-modal/epgu-cf-ui-cta-modal/div[2]/ng-scrollbar/" +
//                "div/div/div/div/div/div/div/div/lib-button[1]/div/button"))
//                .click();
        Selenide.sleep(3000);

        // Снимаем скриншот страницы
        Screenshot actualScreenshot = new AShot()
                .shootingStrategy(ShootingStrategies.viewportPasting(200))
                .shootingStrategy(ShootingStrategies.scaling(1.25f))
                .coordsProvider(new WebDriverCoordsProvider())
                .imageCropper(new IndentCropper()) // Обрезаем изображение, если требуется
                .takeScreenshot(WebDriverRunner.getWebDriver());

//        // Загружаем эталонный скриншот
//        BufferedImage expectedImage = ImageIO.read(new File(
//                "C:\\Users\\denis.buyanov\\Downloads\\DesignTest\\src\\test\\resources" +
//                        "\\expected_screenshots\\expected_design_1920px.png"));

        // Открываем вторую веб-страницу
        open("https://l92.epgu-front.test.gosuslugi.ru/600103/1/form");
        // Если есть черновик - раскомментить чтобы продолжить
//        $(By.xpath("//*[@id='print-page']/portal-new-sf-player/epgu-cf-ui-modal-container/div/" +
//                "epgu-constructor-confirmation-modal/epgu-cf-ui-cta-modal/div[2]/ng-scrollbar/" +
//                "div/div/div/div/div/div/div/div/lib-button[1]/div/button"))
//                .click();
        Selenide.sleep(3000);

        // Снимаем скриншот страницы
        Screenshot expectedImage = new AShot()
                .shootingStrategy(ShootingStrategies.viewportPasting(200))
                .shootingStrategy(ShootingStrategies.scaling(1.25f))
                .coordsProvider(new WebDriverCoordsProvider())
                .imageCropper(new IndentCropper()) // Обрезаем изображение, если требуется
                .takeScreenshot(WebDriverRunner.getWebDriver());

        // Создаем объект для сравнения скриншотов
        ImageDiffer imageDiffer = new ImageDiffer();

        // Устанавливаем политику выделения различий (цвет выделения)
        imageDiffer.withDiffMarkupPolicy(new ImageMarkupPolicy().withDiffColor(Color.RED));

        // Сравниваем скриншоты
        ImageDiff diff = imageDiffer.makeDiff(expectedImage.getImage(), actualScreenshot.getImage());

        // Проверяем, есть ли различия между скриншотами
        if (diff.getDiffSize() != 0) {
            // Если есть различия, добавляем метку в отчет Allure
            Allure.label("testType", "screenshotDiff");

            // Прикрепляем скриншоты к отчету Allure
            attachImg("expected", expectedImage.getImage());
            attachImg("actual", actualScreenshot.getImage());

            // Прикрепляем выделенное изображение с различиями к отчету Allure
            attachImg("diff", diff.getMarkedImage());

            // Прикрепляем прозрачное изображение с различиями к отчету Allure
            attachImg("diff (прозрачность)", diff.getTransparentMarkedImage());

            // Проверяем, что отличий нет
            Assertions.assertAll(
                    () -> assertTrue(diff.hasDiff(), "Дизайн страницы не соответствует эталону. " +
                            "Обнаружены отличия."),
                    () -> assertEquals(0, diff.getDiffSize(), "Количество отличий больше нуля.")
            );
        }
    }
    // Метод для прикрепления изображения к отчету Allure
    public static void attachImg(String name, BufferedImage image) {
        try {
            // Преобразуем изображение в массив байтов
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // Прикрепляем изображение к отчету Allure
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(imageBytes), "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @AfterEach
    public void teardown() {
        closeWebDriver();
    }
}
