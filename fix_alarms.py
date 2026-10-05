import re

path = "lib/main.dart"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Додаємо функцію рекурсивного розкладу
schedule_code = """
@pragma('vm:entry-point')
Future<void> scheduleNextBackgroundUpdate() async {
  await AndroidAlarmManager.initialize();
  DateTime now = DateTime.now();
  
  DateTime nextHour = DateTime(now.year, now.month, now.day, now.hour).add(const Duration(hours: 1, minutes: 1));
  
  DateTime next2022 = DateTime(now.year, now.month, now.day, 2, 40);
  if (!next2022.isAfter(now)) next2022 = next2022.add(const Duration(days: 1));
  
  DateTime next2014 = DateTime(now.year, now.month, now.day, 12, 0);
  if (!next2014.isAfter(now)) next2014 = next2014.add(const Duration(days: 1));

  List<DateTime> times = [nextHour, next2022, next2014];
  times.sort();
  
  await AndroidAlarmManager.oneShotAt(
    times.first,
    2,
    backgroundUpdate,
    exact: true,
    wakeup: true,
  );
}
"""

if "scheduleNextBackgroundUpdate" not in content:
    content = content.replace("void main() async {", schedule_code + "\nvoid main() async {")

# 2. Очищаємо main() від старих periodic таймерів
main_pattern = re.compile(r"await AndroidAlarmManager\.initialize\(\);.*?(?=WidgetsFlutterBinding\.ensureInitialized\(\);)", re.DOTALL)
content = main_pattern.sub("await AndroidAlarmManager.initialize();\n  await scheduleNextBackgroundUpdate();\n  ", content)

# 3. Додаємо рекурсивний виклик (естафету) в кінець backgroundUpdate
if "finally {\n    await scheduleNextBackgroundUpdate();" not in content:
    content = re.sub(r"(\}\s*catch\s*\([^)]*\)\s*\{[^}]*\})", r"\1\n  finally {\n    await scheduleNextBackgroundUpdate();\n  }", content)

# 4. Видаляємо зламаний callbackDispatcher
start_idx = content.find("void callbackDispatcher()")
if start_idx != -1:
    brace_count = 0
    in_func = False
    for i in range(start_idx, len(content)):
        if content[i] == '{':
            brace_count += 1
            in_func = True
        elif content[i] == '}':
            brace_count -= 1
        if in_func and brace_count == 0:
            content = content[:start_idx] + content[i+1:]
            break

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print("✅ УСПІХ: main.dart переведено на рекурсивні oneShotAt таймери!")
