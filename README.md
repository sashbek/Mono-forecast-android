# Mono-forecast-android
Mono forecast is an android app to predict weather for EUC, motocycle and bike riders. It is also supposed to be able track your location and connect some EUCs like [Wheellog](https://github.com/Wheellog/Wheellog.Android).

# Functions overview
* App gets forecast from public API and visualise it in the app
* App analyzes forecasts and calculates the level of difficulty for activities
* Custom view style is controled with settings
* App can track your location if you need it. Weather data will also be tracked with your location
* App can automaticly define your location using GNSS systems the location also can be provided by user
* You can view your saved tracks in the app or export it and open wuth another device

# Development

## Sprint 2 (19.02 to 26.02)

### Result

* Logs page must contain brief labels of saved log files as a list with small data (mocked). Ref: 
  
<img src="./readmi_img\image2.png" alt="Wheellog logs page" width="200"/>

* WeatherUpdateService must run correctly and should be mocked with random temperature updates one time per second.

## Sprint 1 (12.02 to 19.02)

### Result

* 3 or 4 screen application with following pages: Main page, Settings page, Forecast page and (optional) Log page. - DONE
* Main page must contain: header (with page label and settings button), 4 sqare sections lower (Forecast with cloud/sun/etc icon, Bluetooth connect with BLE symbol icon, Location with standart point-on-map icon, logs page with some icon) square icons are supposed to use Fragments. Ref: 

<img src="./readmi_img\Screenshot_20260212-202757.png" alt="Macrodroid main page" width="200"/>
- DONE

* Settings page must contain: header (with page label and settings button), "use system theme" checkbox, light|dark theme switch (disabled if checkbox is marked), API dropout choice list, activity dropout choice list (EUC, Bike, Motorcycle, etc). Theme is supposed to change as soon as switch is clicked by user. Ref: 
  
<img src="./readmi_img\image1.png" alt="Wheellog settings page" width="200"/>
-DONE (with minor differences)

* Forecast page must contain: header (with page label and settings button), weather icon, temperature, wind, humidity, etc data (mocked).
* Logs page must contain brief labels of saved log files as a list with small data (mocked). Ref: 
  
<img src="./readmi_img\image2.png" alt="Wheellog logs page" width="200"/>
- NOT DONE

* Services should be used somehow in the app :)
- NOT DONE

### Done

All except:
* Fragments usage in Logs page.
* Correct Logs page content
* Background service correct work without exception

### Tools to use
* only Kotlin
* only [XML](https://developer.android.com/develop/ui/views/layout/declaring-layout?hl=ru)
* [retrofit](https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet#0) 2 or/and [okhttp](https://habr.com/ru/articles/717900/)

### Sources
[Youtube](https://www.youtube.com/playlist?list=PLgPRahgE-GctUcLMcQFvl00xsXqpNJOix) 

### UML architecture

Build:

<img src="./readmi_img/android-monoforecast.svg" alt="Arch" width="800"/>


Source:

```plantuml
@startuml
!theme plain
skinparam componentStyle rectangle
skinparam backgroundColor #FEFEFE
skinparam component {
  BackgroundColor #E1F5FE
  BorderColor #0288D1
  FontColor #01579B
}
skinparam database {
  BackgroundColor #FFF9C4
  BorderColor #FBC02D
  FontColor #B26A00
}
skinparam interface {
  BackgroundColor #C8E6C9
  BorderColor #2E7D32
  FontColor #1B5E20
}
skinparam control {
  BackgroundColor #FFE0B2
  BorderColor #E65100
  FontColor #BF360C
}
skinparam rectangle {
  BackgroundColor #F3E5F5
  BorderColor #7B1FA2
  FontColor #4A148C
}
skinparam note {
  BackgroundColor #FFFACD
  BorderColor #B8860B
}

title "Mono-forecast-android — Sprint 1 Architecture\n(12.02–19.02)\nАктивности как основные экраны + фрагмент для деталей лога"

' ========== LAYERS ==========

package "UI Layer (Activities + Fragment)" {
  [MainActivity] <<activity>>
  [ForecastActivity] <<activity>>
  [SettingsActivity] <<activity>>
  [LogsActivity] <<activity>>
  [LogDetailFragment] <<fragment>>
}

note right of [LogDetailFragment]
  открывается при клике
  на лог в LogsActivity
end note

package "ViewModel Layer" {
  [MainViewModel] <<viewmodel>>
  [ForecastViewModel] <<viewmodel>>
  [SettingsViewModel] <<viewmodel>>
  [LogsViewModel] <<viewmodel>>
  [LogDetailViewModel] <<viewmodel>>
}

package "Service Layer" {
  [WeatherSyncService] <<service>>
  [LocationTrackingService] <<service>>
  [VehicleConnectionService] <<service>>
}

note right of [VehicleConnectionService]
  для будущих спринтов
end note

package "Repository Layer" {
  [ForecastRepository] <<repository>>
  [SettingsRepository] <<repository>>
  [LogRepository] <<repository>>
}

note right of [ForecastRepository]
  только прогнозы (API)
end note

note right of [SettingsRepository]
  настройки приложения
end note

note right of [LogRepository]
  центральный репозиторий логов
end note

package "Models" {
  
  package "DTO" {
    component "WeatherResponseDto" <<dto>>
    component "ForecastDto" <<dto>>
    component "WindDto" <<dto>>
    component "MainDto" <<dto>>
  }
  
  package "LogFrame" {
    component "LogFrameEntity" <<entity>>
    component "LocationBlockEntity" <<entity>>
    component "WeatherBlockEntity" <<entity>>
    component "DeviceMetricsBlockEntity" <<entity>>
    
    [LogFrameEntity] *-- [LocationBlockEntity] : "0..1"
    [LogFrameEntity] *-- [WeatherBlockEntity] : "0..1"
    [LogFrameEntity] *-- [DeviceMetricsBlockEntity] : "0..1"
  }
}

note top of [LogFrameEntity]
  Каждый лог представляет собой
  временную метку + опциональные блоки:
  • LocationBlock (координаты, скорость)
  • WeatherBlock (темп, ветер, влажность)
  • DeviceMetricsBlock (данные EUC)
  
  Блоки могут сохраняться независимо
  в зависимости от активных сервисов
end note

note right of [LocationBlockEntity]
  optional
end note

note right of [WeatherBlockEntity]
  optional
end note

note right of [DeviceMetricsBlockEntity]
  optional
end note

package "Local Storage" {
  database "Room Database" <<db>> {
    component "LogFrameDao" <<dao>>
    component "SettingsDao" <<dao>>
  }
}

' ========== RELATIONS ==========

' Activity → ViewModel
[MainActivity] --> [MainViewModel]
[ForecastActivity] --> [ForecastViewModel]
[SettingsActivity] --> [SettingsViewModel]
[LogsActivity] --> [LogsViewModel]

' Fragment → ViewModel
[LogDetailFragment] --> [LogDetailViewModel]

' ViewModel → Repository
[MainViewModel] --> [ForecastRepository]
[MainViewModel] --> [LogRepository]
[ForecastViewModel] --> [ForecastRepository]
[SettingsViewModel] --> [SettingsRepository]
[LogsViewModel] --> [LogRepository]
[LogDetailViewModel] --> [LogRepository]

' Service → Repository
[WeatherSyncService] --> [ForecastRepository]
[LocationTrackingService] --> [LogRepository]
[VehicleConnectionService] --> [LogRepository]

' Repository dependencies
[ForecastRepository] --> [WeatherResponseDto]
[ForecastRepository] --> [WeatherSyncService]

[LogRepository] --> [LogFrameDao]
[LogRepository] --> [LogFrameEntity]
[LogRepository] --> [LocationBlockEntity]
[LogRepository] --> [WeatherBlockEntity]
[LogRepository] --> [DeviceMetricsBlockEntity]

[SettingsRepository] --> [SettingsDao]

' API связи
[WeatherSyncService] --> [WeatherResponseDto]
[WeatherResponseDto] *-- [MainDto]
[WeatherResponseDto] *-- [WindDto]

' DAO связи
[LogFrameDao] ..> [LogFrameEntity]
[SettingsDao] ..> [SettingsEntity]

note right of [SettingsEntity]
  <<entity>>
  настройки темы, API,
  типов активности
end note

' Навигация между Activity
[MainActivity] --> [ForecastActivity] : "startActivity"
[MainActivity] --> [SettingsActivity] : "startActivity"
[MainActivity] --> [LogsActivity] : "startActivity"

[LogsActivity] --> [LogDetailFragment] : "открывает в контейнере"

@enduml
```