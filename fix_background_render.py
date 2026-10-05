main_path = 'lib/main.dart'
with open(main_path, 'r', encoding='utf-8') as f:
    code = f.read()

new_bg_func = """@pragma('vm:entry-point')
void backgroundUpdate() async {
  WidgetsFlutterBinding.ensureInitialized();
  DartPluginRegistrant.ensureInitialized();
  final prefs = await SharedPreferences.getInstance();

  bool show2022 = prefs.getBool('show2022') ?? true;
  bool show2014 = prefs.getBool('show2014') ?? true;
  bool showDaysOnly = prefs.getBool('showDaysOnly') ?? false;
  bool showHour = prefs.getBool('showHour') ?? true;

  String time2022 = getGlobalAccurateTime(DateTime(2022, 2, 24, 2, 40), showDaysOnly, showHour);
  String time2014 = getGlobalAccurateTime(DateTime(2014, 2, 20, 12, 0), showDaysOnly, showHour);

  double fontSize = prefs.getDouble('fontSize') ?? 14.0;
  double strokeWidth = prefs.getDouble('strokeWidth') ?? 2.0;
  double opacity = prefs.getDouble('opacity') ?? 0.5;

  int br = prefs.getInt('br') ?? 0;
  int bg = prefs.getInt('bg') ?? 0;
  int bb = prefs.getInt('bb') ?? 0;

  int tr = prefs.getInt('tr') ?? 255;
  int tg = prefs.getInt('tg') ?? 255;
  int tb = prefs.getInt('tb') ?? 255;

  int sr = prefs.getInt('sr') ?? 0;
  int sg = prefs.getInt('sg') ?? 0;
  int sb = prefs.getInt('sb') ?? 0;

  String? imagePath = prefs.getString('imagePath');

  try {
    await HomeWidget.renderFlutterWidget(
      TimeOfWarWidgetRender(
        show2022: show2022,
        show2014: show2014,
        time2022: time2022,
        time2014: time2014,
        fontSize: fontSize * 2.5,
        strokeWidth: strokeWidth * 2.5,
        opacity: opacity,
        bgColor: Color.fromRGBO(br, bg, bb, opacity),
        textColor: Color.fromRGBO(tr, tg, tb, 1.0),
        strokeColor: Color.fromRGBO(sr, sg, sb, 1.0),
        imagePath: imagePath,
      ),
      key: 'widget_image',
      logicalSize: const Size(800, 400),
    );

    await HomeWidget.updateWidget(name: 'WidgetProvider', androidName: 'WidgetProvider');
  } catch (e) {
    // Фоновий рендеринг
  }
}"""

start_idx = code.find("void backgroundUpdate()")
if start_idx != -1:
    pragma_idx = code.rfind("@pragma", 0, start_idx)
    if pragma_idx != -1 and (start_idx - pragma_idx < 80):
        start_idx = pragma_idx
    
    end_idx = code.find('await HomeWidget.updateWidget(name: "WidgetProvider", androidName: "WidgetProvider");', start_idx)
    if end_idx != -1:
        end_bracket = code.find("}", end_idx)
        if end_bracket != -1:
            code = code[:start_idx] + new_bg_func + code[end_bracket+1:]
            with open(main_path, 'w', encoding='utf-8') as f:
                f.write(code)
            print("УСПІХ: backgroundUpdate() переписано на генерацію PNG-зображення!")
        else:
            print("ПОМИЛКА: не знайдено закриваючу дужку.")
    else:
        print("ПОМИЛКА: не знайдено updateWidget усередині backgroundUpdate.")
else:
    print("ПОМИЛКА: функцію backgroundUpdate() не знайдено.")
