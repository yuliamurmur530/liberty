# Подпись Liberty

Официальный package name: `net.crinoidea.liberty`.

Production signing key хранится отдельно от проекта и сервера. Gradle читает путь, alias и пароли только из переменных окружения:

- `LIBERTY_KEYSTORE_PATH`
- `LIBERTY_KEYSTORE_PASSWORD`
- `LIBERTY_KEY_ALIAS`
- `LIBERTY_KEY_PASSWORD`

В репозиторий запрещено добавлять keystore, recovery-файл, DPAPI-credential и пароли. Публичный PEM-сертификат и его SHA-256 fingerprint не являются секретами и могут публиковаться.

На этом компьютере повторная подписанная сборка выполняется отдельным скриптом из закрытого каталога `C:\projects\Liberty-signing`. DPAPI-credential расшифровывается только текущей учётной записью Windows.

Одноразовый recovery-файл следует перенести в надёжный менеджер паролей, проверить сохранённую запись и удалить с диска. Для публикации в Google Play с той же подписью на других каналах нужно выбрать импорт собственного app signing key в Play App Signing; позднее для загрузок создаётся отдельный upload key.
