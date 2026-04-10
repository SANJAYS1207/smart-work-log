# Smart Work Log Tracker 📊

A production-ready, Telegram-based personal productivity and work-logging system built with **Spring Boot 3**, **MySQL**, and **Apache POI**.

---

## ⚡ Quick Setup

### 1. Fill in credentials — `src/main/resources/application.properties`

| Property | Where to get it |
|---|---|
| `telegram.bot.token` | [@BotFather](https://t.me/BotFather) → create bot → copy token |
| `telegram.chat.id` | [@userinfobot](https://t.me/userinfobot) → send /start → copy your ID |
| `spring.datasource.url` | Your cloud MySQL JDBC URL |
| `spring.datasource.username` | DB user |
| `spring.datasource.password` | DB password |

---

### 2. Build

```bash
mvn clean package -DskipTests
```

This produces `target/smartlog-1.0.0.jar`.

---

### 3. Run locally (for testing)

```bash
java -jar target/smartlog-1.0.0.jar
```

App starts on **port 8080**.

---

### 4. Deploy to Render

1. Push this repo to GitHub.
2. Create a new **Web Service** on [Render](https://render.com).
3. Set build command: `mvn clean package -DskipTests`
4. Set start command: `java -jar target/smartlog-1.0.0.jar`
5. Add all properties as **Environment Variables** (safer than editing `application.properties` directly):
   - `TELEGRAM_BOT_TOKEN`
   - `TELEGRAM_CHAT_ID`
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
6. Note your Render URL: `https://<app-name>.onrender.com`

> If using env vars, update `application.properties` like:
> ```properties
> telegram.bot.token=${TELEGRAM_BOT_TOKEN}
> telegram.chat.id=${TELEGRAM_CHAT_ID}
> spring.datasource.url=${SPRING_DATASOURCE_URL}
> ```

---

### 5. Register Telegram Webhook

After deployment, open in browser (once):

```
https://api.telegram.org/bot<YOUR_TOKEN>/setWebhook?url=https://<your-app>.onrender.com/webhook
```

✅ You should see: `{"ok":true,"result":true}`

---

### 6. Set Up External Cron Jobs (cron-job.org)

Go to [cron-job.org](https://cron-job.org), timezone **Asia/Kolkata (IST)**, and create:

| Time | URL | Description |
|---|---|---|
| 09:00 daily | `GET /send-morning-message` | Morning motivation |
| 10:30 daily | `GET /send-reminder` | Start hourly reminders |
| 11:30 daily | `GET /send-reminder` | |
| 12:30 daily | `GET /send-reminder` | |
| 13:30 daily | `GET /send-reminder` | |
| 14:30 daily | `GET /send-reminder` | |
| 15:30 daily | `GET /send-reminder` | |
| 16:30 daily | `GET /send-reminder` | |
| 17:30 daily | `GET /send-reminder` | |
| 18:30 daily | `GET /send-reminder` | |
| Every 15 min | `GET /check-pending` | Pending re-reminder |
| 18:45 daily | `GET /send-day-summary` | End of day |

---

## 📡 API Reference

| Method | Endpoint | Description |
|---|---|---|
| POST | `/webhook` | Telegram webhook receiver |
| GET | `/send-morning-message` | Morning message (9 AM) |
| GET | `/send-reminder` | Hourly reminder + create PENDING |
| GET | `/check-pending` | Re-remind if slot still PENDING |
| GET | `/send-day-summary` | Evening check-in (6:45 PM) |
| GET | `/export-report?month=April 2026` | Manual report trigger |

---

## 💬 Bot Commands (via Telegram)

| You type | Bot does |
|---|---|
| Any text | Saves as work log for current hour slot ✅ |
| `report` | Asks for month/year |
| `April 2026` (after report) | Sends Excel file 📊 |

---

## 🗄️ Cloud MySQL Providers (Free Tier)

| Provider | Free Plan |
|---|---|
| **Railway** | 500 MB, easy setup |
| **Aiven** | 5 GB, generous |
| **PlanetScale** | 5 GB (MySQL-compatible) |
| **Clever Cloud** | 5 MB (very small) |

**Recommendation**: Use [Railway](https://railway.app) → New Project → MySQL → copy JDBC URL.

---

## 📁 Project Structure

```
smart_log/
├── Dockerfile
├── pom.xml
├── schema.sql
└── src/main/
    ├── java/com/sanjay/smartlog/
    │   ├── SmartLogApplication.java
    │   ├── controller/
    │   │   ├── WebhookController.java      ← /webhook
    │   │   ├── ReminderController.java     ← /send-reminder, /check-pending
    │   │   ├── MorningController.java      ← /send-morning-message
    │   │   ├── SummaryController.java      ← /send-day-summary
    │   │   └── ReportController.java       ← /export-report
    │   ├── entity/
    │   │   ├── WorkLog.java
    │   │   └── UserState.java
    │   ├── repository/
    │   │   ├── WorkLogRepository.java
    │   │   └── UserStateRepository.java
    │   └── service/
    │       ├── TelegramService.java        ← sendMessage, sendDocument
    │       ├── WorkLogService.java         ← DB operations
    │       ├── ReportService.java          ← Excel generation
    │       └── ConversationStateService.java ← State machine
    └── resources/
        └── application.properties
```
