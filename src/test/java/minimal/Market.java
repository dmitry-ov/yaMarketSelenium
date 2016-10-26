package minimal;

import static org.junit.Assert.*;

import org.junit.*;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class Market {

    private FirefoxDriver driver;
    String URL = "https://market.yandex.ru/";

    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("version", "latest");
        capabilities.setCapability("platform", Platform.WINDOWS);
        capabilities.setCapability("name", "Testing Selenium");

        System.setProperty("webdriver.gecko.driver", "/Applications/Firefox.app/Contents/MacOS/firefox-bin");

        this.driver = new FirefoxDriver(capabilities);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
    }


    @Test
    public void testYaMarket() throws Exception {
        driver.get(URL);
        assertEquals(URL, this.driver.getCurrentUrl());


//        Нажать по ссылке "Каталог"
//        Открыт "Каталог", представлены блоки: основные категории товаров, "Популярные товары",
//        "Вас также могут заинтересовать", "Статьи и подборки"
        WebElement catalogLink = driver.findElement(By.xpath("//span[contains(.,'Каталог')]"));
        catalogLink.click();
        assertEquals(this.driver.getCurrentUrl(), URL + "?tab=catalog");

        String mainCategory = "//div[@class='catalog-simple__item']";
        int mainCategoriesCount = 12;
        List<WebElement> mainCategories = driver.findElementsByXPath(mainCategory);

        assertEquals(mainCategories.size(), mainCategoriesCount);
        mainCategories.get(mainCategoriesCount - 1).sendKeys(Keys.PAGE_DOWN);

        String articlesSection = "//div[@class='snippet-list snippet-list_type_grid snippet-list_size_4']";
        String interestSection = "//div[@class='snippet-list snippet-list_size_4 flex-grid__list']";
        String popularSection = "//div[@class='snippet-list snippet-list_type_grid snippet-list_size_4 metrika i-bem metrika_js_inited']";

        List<String> sections = Arrays.asList(articlesSection, interestSection, popularSection);
        for (String x : sections) checkXpathElement(x);


//        Перейти в раздел "Электроника" -> "Мобильные телефоны"
//        Открыт раздел "Мобильные телефоны", в категории "Популярные" и "Новинки" представлены 3 девайса
        String electronic = "Электроника";
        String mobilePhones = "Мобильные телефоны";
        driver.findElementByLinkText(electronic).click();
        driver.findElementByLinkText(mobilePhones).click();
        assertTrue(driver.getTitle().contains(mobilePhones));

        List<String> categories = Arrays.asList("Популярные", "Новинки");
        for (String name : categories) {
            String query = "//div[@class='top-3-models'][contains(.,'" + name + "')]";
            checkXpathElement(query);

            int devicesCount = 3;
            String partPath = "/ul/li";
            assertEquals(driver.findElementsByXPath(query + partPath).size(), devicesCount);
        }


//        Нажать по "расширенный поиск" блока выбора по параметрам
        driver.findElementByLinkText("Расширенный поиск →").click();
        assertTrue(driver.getTitle().contains(mobilePhones));
        String paramsBlock = "//div[@class='layout__col layout__col_search-filters_visible i-bem']";
        assertTrue(driver.findElementByXPath(paramsBlock).isDisplayed());


//        Ввести Цену, руб. "от" значение 5125
//        Ввести Цену, руб. "до" значение 10123
        HashMap<String, String> prices = new HashMap<String, String>();
        prices.put("glf-pricefrom-var", "5125");
        prices.put("glf-priceto-var", "10123");

        for (Map.Entry<String, String> x : prices.entrySet()) {
            driver.findElementById(x.getKey()).sendKeys(x.getValue());
            assertEquals(driver.findElementById(x.getKey()).getAttribute("value"), x.getValue());
        }

        WebElement checkBoxOnStock = driver.findElementById("glf-onstock-select");
        if (!checkBoxOnStock.isSelected()) {
            checkBoxOnStock.click();
        }
        assertTrue(checkBoxOnStock.isSelected());


//        Раскрыть блок "Тип"
        String typePhone = "//span[@class='title__content'][contains(.,'Тип')]";
        String itemSmartphone = "//label[@class='checkbox__label'][contains(.,'смартфон')]";

        if (!driver.findElementByXPath(itemSmartphone).isDisplayed()) {
            driver.findElement(By.xpath(typePhone)).click();
        }
        driver.findElementByXPath(itemSmartphone).click();
        checkSelected(itemSmartphone);


//        Кликнуть на селектбокс "Android"
        String itemAndroid = "//label[@class='checkbox__label'][contains(.,'Android')]";
        WebElement checkboxAndroid = driver.findElementByXPath(itemAndroid);
        checkboxAndroid.click();
        checkSelected(itemAndroid);


//        Случано выбрать 3 устройства из представленных на странице, имеющих рейтинг от "3,5" до "4,5",
//      и вывести в лог информацию в формате
//      "номер девайса на странице - наименование девайса - стоимость девайса (от-до)"
        String itemDevice = "//div[@class='n-snippet-card snippet-card clearfix i-bem n-snippet-card_js_inited']";
        List<WebElement> list = driver.findElementsByXPath(itemDevice);
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < list.size() - 1; i++) {
            String[] args = list.get(i).getText().split("\n");
            Double rate = Double.valueOf(args[0]);
            if ((rate <= 4.5) && (rate >= 3.5)) {
                String name = args[3];
                String wordForDelete = "Новинка";
                if (name.toString().contains(wordForDelete)) {
                    name = name.replace(wordForDelete, "");
                }
                String separator = " - ";
                result.add("Номер на странице " + (++i) + separator + name + separator + args[1] + separator + args[2]);
            }
        }

        while (result.size() > 3) {
            Random rand = new Random();
            int n = rand.nextInt(result.size());
            result.remove(n);
        }

        for (String x : result) {
            System.out.println(x);
        }

    }

    private void checkSelected(String queryCheckbox) {
        assertEquals(driver.findElementsByXPath(queryCheckbox + "/parent::*").size(), 1);
        assertTrue(driver.findElementsByXPath(queryCheckbox + "/parent::*").get(0).
                getAttribute("class").contains("checkbox_checked_yes"));
    }


    private void checkXpathElement(String xpath) {
        assertTrue(driver.findElement(By.xpath(xpath)).isDisplayed());
    }

    @After
    public void tearDown() throws Exception {
        this.driver.quit();
    }
}
