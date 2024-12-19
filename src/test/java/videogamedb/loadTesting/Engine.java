package videogamedb.loadTesting;

import io.gatling.app.Gatling;
// Импорт класса GatlingPropertiesBuilder для настройки свойств Gatling
import io.gatling.core.config.GatlingPropertiesBuilder;

// Основной класс для запуска симуляции Gatling
public class Engine {

    // Точка входа в программу
    public static void main(String[] args) {
        // Создание и настройка объекта GatlingPropertiesBuilder
        GatlingPropertiesBuilder props = new GatlingPropertiesBuilder()
                // Установка директории ресурсов
                .resourcesDirectory(IDEPathHelper.mavenResourcesDirectory.toString())
                // Установка директории для результатов
                .resultsDirectory(IDEPathHelper.resultsDirectory.toString())
                // Установка директории для бинарных файлов
                .binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString());

        // Запуск Gatling с настроенными свойствами
        Gatling.fromMap(props.build());
    }
}
