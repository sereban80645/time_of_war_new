path = "lib/main.dart"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Відновлюємо точку входу main()
main_func = """
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await AndroidAlarmManager.initialize();
  await scheduleNextBackgroundUpdate();
  runApp(const MyApp());
}
"""

# Якщо main() втрачено або пошкоджено, вставляємо його перед class MyApp
if "void main()" not in content:
    my_app_idx = content.find("class MyApp")
    if my_app_idx != -1:
        content = content[:my_app_idx] + main_func + "\n\n" + content[my_app_idx:]
    else:
        content += "\n\n" + main_func

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print("✅ main() успішно відновлено у lib/main.dart!")
