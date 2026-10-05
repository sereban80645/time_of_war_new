import re

with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

# 1. Перевіряємо наявність необхідних імпортів
imports = [
    "import 'dart:async';",
    "import 'dart:ui' as ui;",
    "import 'package:flutter/services.dart';"
]
for imp in imports:
    if imp not in code:
        code = imp + "\n" + code

# 2. Додаємо виклик примусового передзавантаження картинки у рендер-функцію
old_render_call = re.search(r"await HomeWidget\.renderFlutterWidget\(.*?\);", code, re.DOTALL)

new_render_logic = """
    // Гарантуємо ініціалізацію зв'язок у фоновому покроці
    WidgetsFlutterBinding.ensureInitialized();

    // Якщо є шлях до зображення, чекаємо його повного декодування перед знімком
    if (imagePath != null && File(imagePath).existsSync()) {
      final completer = Completer<void>();
      final imageStream = MemoryImage(File(imagePath).readAsBytesSync()).resolve(const ImageConfiguration());
      late ImageStreamListener listener;
      listener = ImageStreamListener((_, __) {
        if (!completer.isCompleted) completer.complete();
        imageStream.removeListener(listener);
      }, onError: (_, __) {
        if (!completer.isCompleted) completer.complete();
        imageStream.removeListener(listener);
      });
      imageStream.addListener(listener);
      await completer.future.timeout(const Duration(milliseconds: 500), onTimeout: () {});
      await Future.delayed(const Duration(milliseconds: 100));
    }

    await HomeWidget.renderFlutterWidget(
      const TimeOfWarWidgetRender(),
      key: 'filename',
      logicalSize: const Size(320, 160),
    );"""

if old_render_call:
    code = code.replace(old_render_call.group(0), new_render_logic)

with open("lib/main.dart", "w", encoding="utf-8") as f:
    f.write(code)

print("✅ Глибоку перевірку та виправлення рендерингу застосовано!")
