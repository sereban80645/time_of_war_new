import re

with open("lib/main.dart", "r", encoding="utf-8") as f:
    code = f.read()

# 1. Додаємо перевірку існування файлу в TimeOfWarWidgetRender
render_pattern = r"image: imagePath != null\s*\?\s*DecorationImage\(\s*image: FileImage\(File\(imagePath!\)\),\s*fit: BoxFit\.fill, opacity: opacity\)\s*:\s*null"
render_fix = "image: (imagePath != null && File(imagePath!).existsSync())\n              ? DecorationImage(\n                  image: FileImage(File(imagePath!)),\n                  fit: BoxFit.fill, opacity: opacity)\n              : null"

code = re.sub(render_pattern, render_fix, code)

# 2. Додаємо таку ж перевірку в прев'ю віджета
preview_pattern = r"image: _imagePath != null \? DecorationImage\(image: FileImage\(File\(_imagePath!\)\), fit: BoxFit\.fill, opacity: _opacity\) : null"
preview_fix = "image: (_imagePath != null && File(_imagePath!).existsSync()) ? DecorationImage(image: FileImage(File(_imagePath!)), fit: BoxFit.fill, opacity: _opacity) : null"

code = re.sub(preview_pattern, preview_fix, code)

# 3. Виправляємо функцію _pickImage для надійного копіювання та збереження шляху
pick_pattern = re.compile(
    r"Future<void> _pickImage\(\) async \{\s*"
    r"final picker = ImagePicker\(\);\s*"
    r"final pickedFile = await picker\.pickImage\(source: ImageSource\.gallery\);\s*"
    r"if \(pickedFile != null\) \{\s*"
    r"String\? cropped = await _cropImage\(pickedFile\.path\);\s*"
    r"setState\(\(\) => _imagePath = \(cropped \?\? pickedFile\.path\)\);\s*"
    r"_saveSetting\('imagePath', pickedFile\.path\);\s*"
    r"\}\s*\}",
    re.MULTILINE
)

pick_fix = """Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedFile = await picker.pickImage(source: ImageSource.gallery);
    if (pickedFile != null) {
      String? cropped = await _cropImage(pickedFile.path);
      String sourcePath = cropped ?? pickedFile.path;
      
      try {
        // Копіюємо файл у постійну папку додатка, щоб Android його не видалив з кешу
        final appDir = File(sourcePath).parent.path;
        final persistentPath = '$appDir/bg_widget_saved.png';
        final savedFile = await File(sourcePath).copy(persistentPath);
        
        setState(() => _imagePath = savedFile.path);
        _saveSetting('imagePath', savedFile.path);
      } catch (e) {
        setState(() => _imagePath = sourcePath);
        _saveSetting('imagePath', sourcePath);
      }
    }
  }"""

code = pick_pattern.sub(pick_fix, code)

with open("lib/main.dart", "w", encoding="utf-8") as f:
    f.write(code)

print("✅ Збереження фонового зображення виправлено!")
