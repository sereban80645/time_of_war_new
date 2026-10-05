import os

path = "lib/main.dart"
if os.path.exists(path):
    with open(path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    imports = []
    code_lines = []

    for line in lines:
        if line.strip().startswith("import "):
            if line.strip() not in imports:
                imports.append(line.strip())
        else:
            code_lines.append(line)

    new_content = "\n".join(imports) + "\n\n" + "".join(code_lines).lstrip()

    with open(path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("lib/main.dart safely updated!")
else:
    print("File not found!")
