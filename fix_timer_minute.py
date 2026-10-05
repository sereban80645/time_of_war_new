path = "lib/main.dart"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Шукаємо старий рядок щогодинного таймера і замінюємо його на версію із точним стартом на :01 хвилині
old_line = 'await AndroidAlarmManager.periodic(const Duration(hours: 1), 2, backgroundUpdate, exact: true, wakeup: true);'

new_code = '''DateTime nowForHour = DateTime.now();
  DateTime nextHour = DateTime(nowForHour.year, nowForHour.month, nowForHour.day, nowForHour.hour).add(const Duration(hours: 1, minutes: 1));
  if (nextHour.isBefore(nowForHour)) {
    nextHour = nextHour.add(const Duration(hours: 1));
  }
  await AndroidAlarmManager.periodic(const Duration(hours: 1), 2, backgroundUpdate, startAt: nextHour, exact: true, wakeup: true);'''

if old_line in content:
    content = content.replace(old_line, new_code)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Таймер успішно змінено на 01 хвилину!")
else:
    print("Не вдалося знайти рядок таймера, можливо він вже відрізняється.")
