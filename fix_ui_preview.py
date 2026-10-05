import re

with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

# Знаходимо клас _TimeOfWarScreenState, щоб шукати змінні тільки в ньому
match = re.search(r'(class _TimeOfWarScreenState.*?)(class |\Z)', code, re.DOTALL)
if match:
    state_code = match.group(1)
    starts = [m.start() for m in re.finditer(r"TimeOfWarWidgetRender\(", state_code)]
    for start in starts:
        open_parens = 0
        for i in range(start + 22, len(state_code)):
            if state_code[i] == '(':
                open_parens += 1
            elif state_code[i] == ')':
                if open_parens == 0:
                    call_str = state_code[start:i+1]
                    # Якщо це той самий помилковий виклик з tr, tg, tb
                    if "Color.fromRGBO(tr" in call_str or "imagePath: imagePath" in call_str:
                        def get_best_var(candidates, default):
                            for c in candidates:
                                if re.search(r'\b' + c + r'\b', state_code): return c
                            return default

                        show2022 = get_best_var(["_show2022", "show2022"], "true")
                        show2014 = get_best_var(["_show2014", "show2014"], "true")
                        bg = get_best_var(["_bgColor", "bgColor"], "const Color.fromRGBO(0, 0, 0, 0.5)")
                        txt = get_best_var(["_textColor", "textColor"], "const Color.fromRGBO(255, 255, 255, 1.0)")
                        stk = get_best_var(["_strokeColor", "strokeColor"], "const Color.fromRGBO(0, 0, 0, 1.0)")
                        img = get_best_var(["_imagePath", "imagePath"], "null")

                        new_call = f"TimeOfWarWidgetRender(show2022: {show2022}, show2014: {show2014}, bgColor: {bg}, textColor: {txt}, strokeColor: {stk}, imagePath: {img})"
                        
                        new_state_code = state_code.replace(call_str, new_call)
                        code = code.replace(state_code, new_state_code)
                    break
                else:
                    open_parens -= 1

with open("lib/main.dart", "w", encoding="utf-8") as f:
    f.write(code)

print("✅ UI-прев'ю віджета успішно виправлено!")
