import re

path = "lib/main.dart"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Замінюємо помилкове зчитування getInt на безпечне getDouble().toInt()
replacements = {
    r"int br = prefs\.getInt\('br'\) \?\? 0;": "int br = (prefs.getDouble('br') ?? 30.0).toInt();",
    r"int bg = prefs\.getInt\('bg'\) \?\? 0;": "int bg = (prefs.getDouble('bg') ?? 30.0).toInt();",
    r"int bb = prefs\.getInt\('bb'\) \?\? 0;": "int bb = (prefs.getDouble('bb') ?? 30.0).toInt();",
    r"int tr = prefs\.getInt\('tr'\) \?\? 255;": "int tr = (prefs.getDouble('tr') ?? 255.0).toInt();",
    r"int tg = prefs\.getInt\('tg'\) \?\? 255;": "int tg = (prefs.getDouble('tg') ?? 255.0).toInt();",
    r"int tb = prefs\.getInt\('tb'\) \?\? 255;": "int tb = (prefs.getDouble('tb') ?? 255.0).toInt();",
    r"int sr = prefs\.getInt\('sr'\) \?\? 0;": "int sr = (prefs.getDouble('sr') ?? 0.0).toInt();",
    r"int sg = prefs\.getInt\('sg'\) \?\? 0;": "int sg = (prefs.getDouble('sg') ?? 0.0).toInt();",
    r"int sb = prefs\.getInt\('sb'\) \?\? 0;": "int sb = (prefs.getDouble('sb') ?? 0.0).toInt();",
}

for old_code, new_code in replacements.items():
    content = re.sub(old_code, new_code, content)

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print("✅ Помилку типів кольорів успішно виправлено!")
