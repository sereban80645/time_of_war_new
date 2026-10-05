with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

# 1. Примусово оновлюємо пам'ять у фоновому процесі
code = code.replace(
    "SharedPreferences prefs = await SharedPreferences.getInstance();",
    "SharedPreferences prefs = await SharedPreferences.getInstance();\n    await prefs.reload();"
)

# 2. Стискаємо зображення (щоб обійти ліміт в 1МБ)
code = code.replace(
    "picker.pickImage(source: ImageSource.gallery)",
    "picker.pickImage(source: ImageSource.gallery, imageQuality: 40, maxWidth: 800, maxHeight: 800)"
)

with open("lib/main.dart", "w", encoding="utf-8") as f:
    f.write(code)

print("✅ Безпечні зміни успішно застосовано!")
