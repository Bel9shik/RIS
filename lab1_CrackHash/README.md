# CrackHash (lab1)

Распределённый подбор строки по **MD5**: **менеджер** принимает запросы, режет перебор на части и шлёт задачи **воркерам**; воркеры считают свой диапазон комбинаций и отдают результат обратно менеджеру. Очередь: одновременно в работе одна задача, остальные ждут. Уже взломанные пары `(hash, maxLength)` кэшируются — повторные запросы с тем же хешом получают ответ без повторного перебора.

**Модули:** `crackhash-schema` (JAXB/DTO), `crackhash-manager` (REST API), `crackhash-worker` (вычисления).

## Требования

- **Docker** + Docker Compose **или** **JDK 17** + локальный Gradle (`./gradlew`).

## Запуск в Docker

Из каталога `lab1_CrackHash`:

```bash
docker compose up -d --build
```

Поднимутся менеджер на **http://localhost:8080** и три воркера во внутренней сети compose.

Остановка:

```bash
docker compose down
```

Полная пересборка без кэша образов:

```bash
docker compose down && docker compose build --no-cache && docker compose up -d
```

## API менеджера

| Метод | Описание |
|--------|----------|
| `POST /api/hash/crack` | Тело: `{"hash":"<md5>","maxLength":N}` → `{ "requestId": "..." }` |
| `GET /api/hash/status?requestId=<uuid>` | Статус: `QUEUED`, `IN_PROGRESS`, `READY`, `ERROR`; для `READY` — поле `data` со списком слов |
| `GET /api/hash/dictionary` | Снимок in-memory кэша результатов (отладка) |

## Скрипты (`scripts/`)

```bash
chmod +x scripts/createTask.sh scripts/checkStatus.sh
./scripts/createTask.sh                    # создать задачу (хеш внутри скрипта)
./scripts/checkStatus.sh <requestId>       # проверить статус
```

## Локальный запуск без Docker (кратко)

1. Собрать: `./gradlew :crackhash-manager:bootJar :crackhash-worker:bootJar`
2. В `application.properties` менеджера указать URL воркеров (`crackhash.worker.urls`).
3. Запустить **один** экземпляр менеджера и **несколько** воркеров на **разных портах** (`server.port`), у каждого воркера — корректный `crackhash.manager.url`.

Проще для проверки использовать **только Docker Compose**, как выше.

## Полезные переменные (менеджер)

- `crackhash.worker.urls` — список URL воркеров через запятую  
- `crackhash.request.timeout-seconds` — таймаут задачи в секундах (по умолчанию 300)

Воркер: `crackhash.manager.url` — базовый URL менеджера для PATCH с результатом.
