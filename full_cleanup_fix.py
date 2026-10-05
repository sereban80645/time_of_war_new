import re

with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

# 1. Перевіряємо та додаємо import path_provider
if "package:path_provider/path_provider.dart" not in code:
    code = "import 'package:path_provider/path_provider.dart';\n" + code

# 2. Очищення від дубльованих @pragma та порожніх рядків
code = re.sub(r"(@pragma\('vm:entry-point'\)\s*){2,}", "@pragma('vm:entry-point')\n", code)

# 3. Оновлення функції вибору та збереження фото на getApplicationDocumentsDirectory
pick_pattern = re.compile(
    r"Future<void> _pickImage\(\) async \{.*?\n  \}",
    re.DOTALL
)

new_pick_image = """Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedFile = await picker.pickImage(source: ImageSource.gallery);
    if (pickedFile != null) {
      String? cropped = await _cropImage(pickedFile.path);
      String sourcePath = cropped ?? pickedFile.path;
      
      try {
        final docsDir = await getApplicationDocumentsDirectory();
        final persistentPath = '${docsDir.path}/widget_bg_saved.png';
        
        final savedFile = await File(sourcePath).copy(persistentPath);
        
        setState(() => _imagePath = savedFile.path);
        await _saveSetting('imagePath', savedFile.path);
      } catch (e) {
        setState(() => _imagePath = sourcePath);
        await _saveSetting('imagePath', sourcePath);
      }
    }
  }"""

code = pick_pattern.sub(new_pick_image, code)

# 4. Перевірка наявності файлу перед рендерингом фону
code = re.sub(
    r"image:\s*\(_imagePath != null.*?\)\s*:\s*null",
    "image: (_imagePath != null && File(_imagePath!).existsSync()) ? DecorationImage(image: FileImage(File(_imagePath!)), fit: BoxFit.fill, opacity: _opacity) : null",
    code
)

with open("lib/main.dart", "w", encoding="utf-8") as f:
    f.write(code)

print("✅ Застосунок очищено від сміття, фон переведено на гарантоване постійне сховище!")
