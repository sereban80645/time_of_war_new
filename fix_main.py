import re

with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

# Відновлюємо main() та правильну функцію планування
pattern = re.compile(
    r"@pragma\('vm:entry-point'\)\s+"
    r"Future<void> scheduleNextBackgroundUpdate\(\) async \{\s+"
    r"await AndroidAlarmManager\.initialize\(\);\s+"
    r"await scheduleNextBackgroundUpdate\(\);\s+"
    r"WidgetsFlutterBinding\.ensureInitialized\(\);\s+"
    r"runApp\(const MyApp\(\)\);\s+"
    r"\}",
    re.MULTILINE
)

fixed_code = """void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await AndroidAlarmManager.initialize();
  try {
    Workmanager().initialize(callbackDispatcher, isInDebugMode: false);
  } catch (e) {}
  
  runApp(const MyApp());
  scheduleNextBackgroundUpdate();
}

Future<void> scheduleNextBackgroundUpdate() async {
  final now = DateTime.now();
  // Наступне оновлення: рівно о 01 хвилині наступної години
  DateTime nextUpdate = DateTime(now.year, now.month, now.day, now.hour).add(const Duration(hours: 1, minutes: 1));
  
  await AndroidAlarmManager.oneShotAt(
    nextUpdate,
    0,
    backgroundUpdate,
    exact: true,
    wakeup: true,
    allowWhileIdle: true,
  );
}"""

code = pattern.sub(fixed_code, code)

# Прибираємо сміття з порожніх тегів @pragma, залишаючи лише один
code = re.sub(r"(@pragma\('vm:entry-point'\)\s*){2,}", "@pragma('vm:entry-point')\n", code)

with open("lib/main.dart", "w", encoding="utf-8") as f:
    f.write(code)

print("✅ Функцію main() та логіку таймера успішно відновлено!")
