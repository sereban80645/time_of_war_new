import re
import os

path = "lib/main.dart"
if os.path.exists(path):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    import_pattern = r"import\s+['\"][^'\"]+['\"];"
    imports = list(dict.fromkeys(re.findall(import_pattern, content)))

    clean_content = re.sub(import_pattern, "", content).strip()
    new_content = "\n".join(imports) + "\n\n" + clean_content + "\n"

    with open(path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("lib/main.dart imports fixed!")
else:
    print("File not found!")
