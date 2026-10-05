import os, re

path = "lib/main.dart"
if os.path.exists(path):
    with open(path, "r", encoding="utf-8") as f:
        code = f.read()

    # Переконуємося, що фоновий колбек обчислює DateTime.now() під час кожного виклику
    print("Checking main.dart background update status...")

