import re

with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

# 1. Додаємо перезавантаження SharedPreferences у фонових ізолятах
code = re.sub(
    r"(SharedPreferences\s+prefs\s*=\s*await\s+SharedPreferences\.getInstance\(\);)",
    r"\1\n    await prefs.reload();",
    code
)

# 2. Зменшуємо роздільну здатність та якість картинки при виборі (щоб обійти ліміт 1 МБ)
code = re.sub(
    r"picker\.pickImage\(source:\s*ImageSource\.gallery\)",
    "picker.pickImage(source: ImageSource.gallery, imageQuality: 40, maxWidth: 800, maxHeight: 800)",
    code
)

with open("lib/main.dart", "w", encoding="utf-8") as f:
    f.write(code)

print("✅ Виправлено синхронізацію пам'яті та додано стиснення зображення!")
