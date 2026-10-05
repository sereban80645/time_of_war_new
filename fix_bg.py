import re

with open("lib/main.dart", "r") as f:
    code = f.read()

new_bg = """@pragma('vm:entry-point')
void backgroundUpdate() async {
  final prefs = await SharedPreferences.getInstance();
  final showHours = prefs.getBool('show_hours') ?? false;

  final now = DateTime.now();
  final date2014 = DateTime(2014, 2, 20, 0, 0);
  final date2022 = DateTime(2022, 2, 24, 0, 0);

  final diff2014 = now.difference(date2014);
  final diff2022 = now.difference(date2022);

  if (showHours) {
    await HomeWidget.saveWidgetData('text_2014', '${diff2014.inDays} дн. ${diff2014.inHours % 24} год.');
    await HomeWidget.saveWidgetData('text_2022', '${diff2022.inDays} дн. ${diff2022.inHours % 24} год.');
  } else {
    await HomeWidget.saveWidgetData('text_2014', '${diff2014.inDays}');
    await HomeWidget.saveWidgetData('text_2022', '${diff2022.inDays}');
  }

  await HomeWidget.updateWidget(name: 'TimeOfWarWidgetProvider', androidName: 'TimeOfWarWidgetProvider');
}"""

# Видаляємо старий callbackDispatcher або старий backgroundUpdate, якщо вони є
code = re.sub(r'@pragma\(\'vm:entry-point\'\)\s*void\s+callbackDispatcher\(\).*?\n\s*\}\s*\n', '', code, flags=re.DOTALL)
code = re.sub(r'@pragma\(\'vm:entry-point\'\)\s*void\s+backgroundUpdate\(\).*?\n\}\s*', '', code, flags=re.DOTALL)

# Додаємо новий backgroundUpdate на початок файлу перед main
code = new_bg + "\n\n" + code

with open("lib/main.dart", "w") as f:
    f.write(code)

print("ГОТОВО: backgroundUpdate успішно оновлено!")
