with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

correct_call = None
import re
starts = [m.start() for m in re.finditer(r"TimeOfWarWidgetRender\(", code)]

for start in starts:
    open_parens = 0
    # 22 - це довжина рядка "TimeOfWarWidgetRender("
    for i in range(start + 22, len(code)):
        if code[i] == '(':
            open_parens += 1
        elif code[i] == ')':
            if open_parens == 0:
                call_str = code[start:i+1]
                if "show2022" in call_str:
                    correct_call = call_str
                break
            else:
                open_parens -= 1

if correct_call:
    # Замінюємо помилковий порожній виклик на правильний з усіма аргументами
    code = code.replace("const TimeOfWarWidgetRender()", correct_call)
    with open("lib/main.dart", "w", encoding="utf-8") as f:
        f.write(code)
    print("✅ Параметри віджета успішно відновлено!")
else:
    print("❌ Помилка: не знайдено оригінальних параметрів.")
