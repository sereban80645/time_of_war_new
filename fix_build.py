with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

# Замінюємо imagePath на _imagePath! для правильної роботи з приватним станом
code = code.replace(
    "if (imagePath != null && File(imagePath).existsSync()) {",
    "if (_imagePath != null && File(_imagePath!).existsSync()) {"
)

code = code.replace(
    "MemoryImage(File(imagePath).readAsBytesSync())",
    "MemoryImage(File(_imagePath!).readAsBytesSync())"
)

with open("lib/main.dart", "w", encoding="utf-8") as f:
    f.write(code)

print("✅ Назву змінної та null-safety виправлено!")
