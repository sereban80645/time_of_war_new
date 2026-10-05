import os

path = "lib/main.dart"
if os.path.exists(path):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    last_brace = content.rfind('}')
    if last_brace != -1:
        clean_content = content[:last_brace + 1].strip() + "\n"
        with open(path, "w", encoding="utf-8") as f:
            f.write(clean_content)
        print("lib/main.dart successfully cleaned!")
    else:
        print("Closing brace '}' not found!")
else:
    print("File not found!")
