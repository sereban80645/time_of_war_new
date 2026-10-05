import subprocess

# Отримуємо код зі здорового комміту
old_file = subprocess.check_output(['git', 'show', 'a4457309:lib/main.dart'], text=True)
pragma_marker = "@pragma('vm:entry-point')"

if pragma_marker in old_file:
    # Відрізаємо всі фонові функції (від першої прагми і до кінця)
    healthy_tail = pragma_marker + old_file.split(pragma_marker, 1)[1]
    
    # Читаємо поточний зламаний файл
    with open("lib/main.dart", "r", encoding="utf-8") as f:
        current_content = f.read()
        
    # Видаляємо порожні прагми з поточного файлу
    if pragma_marker in current_content:
        clean_top = current_content.split(pragma_marker, 1)[0].rstrip()
    else:
        clean_top = current_content.rstrip()
        
    # Об'єднуємо інтерфейс із відновленими фоновими функціями
    with open("lib/main.dart", "w", encoding="utf-8") as f:
        f.write(clean_top + "\n\n" + healthy_tail)
    print("Фонові функції повністю відновлено!")
else:
    print("Не вдалося знайти фонові функції у старому комміті.")
