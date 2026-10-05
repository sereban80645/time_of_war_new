import os, re

for root, dirs, files in os.walk("lib"):
    for file in files:
        if file.endswith(".dart"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()
            
            new_content = re.sub(r"(\b\w+\.inDays\b)\s*\+\s*1", r"\1 - 1", content)
            if new_content == content:
                new_content = re.sub(r"(\b\w+\.inDays\b)(?!\s*[\+\-]\s*1)", r"(\1 - 1)", content)

            if new_content != content:
                with open(path, "w", encoding="utf-8") as f:
                    f.write(new_content)
                print("Updated:", path)
