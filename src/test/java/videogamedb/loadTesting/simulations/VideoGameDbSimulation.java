package videogamedb.loadTesting.simulations;

// Импорт необходимых классов Gatling для основного функционала и HTTP-протокола
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

// Статический импорт методов DSL Gatling, позволяющий использовать их без префикса класса
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class VideoGameDbSimulation extends Simulation {

    // HTTP-конфигурация для всех запросов в этой симуляции
    // Устанавливает базовый URL, заголовок accept и заголовок типа контента
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");


    // USER_COUNT: количество пользователей для симуляции (по умолчанию: 5)
    private static final int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "5"));

    // RAMP_DURATION: продолжительность наращивания пользователей (по умолчанию: 10 секунд)
    private static final int RAMP_DURATION = Integer.parseInt(System.getProperty("RAMP_DURATION", "10"));

    // Фидер для предоставления тестовых данных из JSON-файла
    /**
     * Фидер в Gatling - это механизм для динамической подачи тестовых данных в сценарий нагрузочного тестирования.
     * Он читает данные из внешнего источника (в вашем случае, из JSON-файла) и предоставляет их виртуальным пользователям
     * во время выполнения теста. Это позволяет создавать более реалистичные сценарии, где каждый пользователь может
     * использовать уникальные данные для своих запросов.*/
    // Случайным образом выбирает данные из файла для каждой итерации
    private static FeederBuilder.FileBased<Object> jsonFeeder = jsonFile("data/gameJsonFile.json").random();

    // Метод, выполняемый перед началом симуляции
    // Выводит информацию о количестве пользователей и продолжительности наращивания
    @Override
    public void before() {
        System.out.printf("Starting test with %d users%n", USER_COUNT);
        System.out.printf("Ramping users over %d seconds%n", RAMP_DURATION);
    }

    // ЧАСТЬ: ШАБЛОНЫ ЗАПРОСОВ (HTTP вызовы)
    // -------------------------------------

    // HTTP-вызов для получения всех видеоигр
    private static ChainBuilder getAllVideoGames =
            exec(http("Get all video games")
                    .get("/videogame"));

    // HTTP-вызов для аутентификации и сохранения JWT-токена
    private static ChainBuilder authenticate =
            exec(http("Authenticate")
                    .post("/authenticate")
                    .body(StringBody("{\n" +
                            "  \"password\": \"admin\",\n" +
                            "  \"username\": \"admin\"\n" +
                            "}"))
                    .check(jmesPath("token").saveAs("jwtToken")));

    // HTTP-вызов для создания новой игры
    // Использует фидер для предоставления данных об игре и сохраненный JWT-токен для авторизации
    private static ChainBuilder createNewGame =
            feed(jsonFeeder)
                    .exec(http("Create New Game - #{name}")
                            .post("/videogame")
                            .header("Authorization", "Bearer #{jwtToken}")
                            .body(ElFileBody("bodies/newGameTemplate.json")).asJson());

    // HTTP-вызов для получения деталей последней добавленной игры
    // Проверяет, соответствует ли возвращенное имя игры ожидаемому имени
    private static ChainBuilder getLastPostedGame =
            exec(http("Get Last Posted Game - #{name}")
                    .get("/videogame/#{id}")
                    .check(jmesPath("name").isEL("#{name}")));

    // HTTP-вызов для удаления последней добавленной игры
    // Использует сохраненный JWT-токен для авторизации
    // Проверяет, указывает ли тело ответа на успешное удаление
    private static ChainBuilder deleteLastPostedGame =
            exec(http("Delete Game - #{name}")
                    .delete("/videogame/#{id}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(bodyString().is("Video game deleted")));


    // ЧАСТЬ: СЦЕНАРИЙ НАГРУЗКИ
    // -------------------------

    // Определение сценария, объединяющего все HTTP-вызовы
    private ScenarioBuilder scn = scenario("Video Game DB Stress Test")
            .exec(getAllVideoGames)       // Получаем список игр
            .pause(2)                     // Пауза 2 секунды
            .exec(authenticate)           // Аутентификация пользователя
            .pause(2)
            .exec(createNewGame)          // Создание новой игры
            .pause(2)
            .exec(getLastPostedGame)      // Получение последней добавленной игры
            .pause(2)
            .exec(deleteLastPostedGame);  // Удаление последней добавленной игры

    // Конфигурация нагрузочной симуляции
    {
        setUp(
                scn.injectOpen(             // Запуск сценария с инъекцией виртуальных пользователей
                        nothingFor(5),       // Начать через 5 секунд
                        rampUsers(USER_COUNT).during(RAMP_DURATION) // Постепенное добавление пользователей
                )
        ).protocols(http.baseUrl("https://videogamedb.uk/api")
                .acceptHeader("application/json")
                .contentTypeHeader("application/json"));
    }
}
