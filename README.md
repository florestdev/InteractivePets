# InteractivePets
Плагин, добавляющий ИИ для прирученных собак, котов и лис. Подходит для социальных и РП проектов.

# Поддержка
Поддерживаются все облачные ИИ-сервисы. К примеру, OpenAI/Gigachat/Yandex.
Используется датабаза HicariCP для производительности.

# КОНФИГ

```
# FLOREST'S PLUGIN ON PETS WITH AI-ASSISTANT IN MINECRAFT
# FOR BUKKIT SERVERS (SPIGOT, BUKKIT, LEAF and OTHERS)
# @florestdev


# AI Settings
ai-key: "your-api-key"
ai-service: "https://api.openai.com/v1/chat/completions"
ai-model: "gpt-3.5-turbo"
ai-system-prompt: "Ты - питомец игрока. Отвечай кратко и дружелюбно, как домашнее животное."

# Pet prices
pet-cost:
  dog: 50
  cat: 50
  parrot: 75
  fox: 100

# Database settings
database:
  host: "localhost"
  port: 3306
  database: "pets"
  username: "root"
  password: "password"
  pool-size: 10


  ```

# Потребности
Bukkit (Paper, Spigot, Leaf, другие) от 1.21 и выше
Vault последней версии
Доступ в Интернет (если не используете ollama и др.)
