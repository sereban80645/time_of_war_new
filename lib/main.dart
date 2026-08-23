import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:home_widget/home_widget.dart';
import 'package:image_cropper/image_cropper.dart';
import 'package:image_picker/image_picker.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF121212),
      ),
      home: const TimeOfWarScreen(),
    );
  }
}

class TimeOfWarWidgetRender extends StatelessWidget {
  final bool show2022;
  final bool show2014;
  final String time2022;
  final String time2014;
  final double fontSize;
  final double strokeWidth;
  final Color bgColor;
  final Color textColor;
  final Color strokeColor;
  final String? imagePath;

  const TimeOfWarWidgetRender({
    super.key,
    required this.show2022,
    required this.show2014,
    required this.time2022,
    required this.time2014,
    required this.fontSize,
    required this.strokeWidth,
    required this.bgColor,
    required this.textColor,
    required this.strokeColor,
    this.imagePath,
  });

  Widget _buildOutlinedText(String text) {
    return Stack(
      children: [
        Text(
          text,
          style: TextStyle(
            fontSize: fontSize,
            fontWeight: FontWeight.bold,
            foreground: Paint()
              ..style = PaintingStyle.stroke
              ..strokeWidth = strokeWidth
              ..color = strokeColor,
          ),
        ),
        Text(
          text,
          style: TextStyle(
            fontSize: fontSize,
            fontWeight: FontWeight.bold,
            color: textColor,
          ),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final hasImage =
        imagePath != null &&
        imagePath!.isNotEmpty &&
        File(imagePath!).existsSync();

    return Container(
      width: 400,
      height: 400,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(56),
        color: hasImage ? null : bgColor,
        image: hasImage
            ? DecorationImage(
                image: FileImage(File(imagePath!)),
                fit: BoxFit.cover,
              )
            : null,
      ),
      child: Container(
        width: double.infinity,
        height: double.infinity,
        alignment: Alignment.center,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(56),
          color: hasImage ? bgColor : null,
        ),
        padding: const EdgeInsets.symmetric(
          vertical: 40,
          horizontal: 40,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            if (show2022) ...[
              Text(
                'Повномасштабна війна:',
                softWrap: false,
                style: TextStyle(
                  color: Colors.white70,
                  fontSize: fontSize * 0.5,
                  fontWeight: FontWeight.w500,
                ),
              ),
              _buildOutlinedText(time2022),
            ],
            if (show2014) ...[
              if (show2022) const SizedBox(height: 20),
              Text(
                'Війна з 2014 року:',
                softWrap: false,
                style: TextStyle(
                  color: Colors.white70,
                  fontSize: fontSize * 0.5,
                  fontWeight: FontWeight.w500,
                ),
              ),
              _buildOutlinedText(time2014),
            ],
          ],
        ),
      ),
    );
  }
}

class TimeOfWarScreen extends StatefulWidget {
  const TimeOfWarScreen({super.key});

  @override
  State<TimeOfWarScreen> createState() => _TimeOfWarScreenState();
}

class _TimeOfWarScreenState extends State<TimeOfWarScreen> {
  bool _show2022 = true;
  bool _show2014 = false;
  bool _showHour = true;
  bool _showDaysOnly = false;

  double _fontSize = 22.0;
  double _strokeWidth = 3.0;
  double _opacity = 0.5;

  double _br = 30.0;
  double _bg = 30.0;
  double _bb = 30.0;

  double _tr = 255.0;
  double _tg = 255.0;
  double _tb = 255.0;

  double _sr = 0.0;
  double _sg = 0.0;
  double _sb = 0.0;

  String? _imagePath;

  Timer? _timer;
  Timer? _debounce;

  final DateTime _date2022Start =
      DateTime(2022, 2, 24, 5, 0);

  final DateTime _date2014Start =
      DateTime(2014, 2, 20, 12, 0);

  @override
  void initState() {
    super.initState();

    _loadSettings();

    _timer = Timer.periodic(
      const Duration(seconds: 1),
      (_) {
        if (!mounted) return;
        setState(() {});
      },
    );
  }

  @override
  void dispose() {
    _timer?.cancel();
    _debounce?.cancel();
    super.dispose();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();

    if (!mounted) return;

    setState(() {
      _show2022 = prefs.getBool('show2022') ?? true;
      _show2014 = prefs.getBool('show2014') ?? false;
      _showHour = prefs.getBool('showHour') ?? true;
      _showDaysOnly = prefs.getBool('showDaysOnly') ?? false;

      _fontSize = prefs.getDouble('fontSize') ?? 22.0;
      _strokeWidth = prefs.getDouble('strokeWidth') ?? 3.0;
      _opacity = prefs.getDouble('opacity') ?? 0.5;

      _br = prefs.getDouble('br') ?? 30.0;
      _bg = prefs.getDouble('bg') ?? 30.0;
      _bb = prefs.getDouble('bb') ?? 30.0;

      _tr = prefs.getDouble('tr') ?? 255.0;
      _tg = prefs.getDouble('tg') ?? 255.0;
      _tb = prefs.getDouble('tb') ?? 255.0;

      _sr = prefs.getDouble('sr') ?? 0.0;
      _sg = prefs.getDouble('sg') ?? 0.0;
      _sb = prefs.getDouble('sb') ?? 0.0;

      _imagePath = prefs.getString('imagePath');
    });

    await _updateHomeWidget();
  }

  Future<void> _saveSetting(
    String key,
    dynamic value,
  ) async {
    final prefs = await SharedPreferences.getInstance();

    if (value is bool) {
      await prefs.setBool(key, value);
    } else if (value is double) {
      await prefs.setDouble(key, value);
    } else if (value is String) {
      await prefs.setString(key, value);
    }

    _debouncedUpdate();
  }

  void _debouncedUpdate() {
    _debounce?.cancel();

    _debounce = Timer(
      const Duration(milliseconds: 500),
      () {
        _updateHomeWidget();
      },
    );
  }

  Color _backgroundColor() {
    return Color.fromRGBO(
      _br.toInt(),
      _bg.toInt(),
      _bb.toInt(),
      _opacity,
    );
  }

  Color _textColor() {
    return Color.fromRGBO(
      _tr.toInt(),
      _tg.toInt(),
      _tb.toInt(),
      1.0,
    );
  }

  Color _strokeColor() {
    return Color.fromRGBO(
      _sr.toInt(),
      _sg.toInt(),
      _sb.toInt(),
      1.0,
    );
  }

  Future<void> _updateHomeWidget() async {
    try {
      final time2022 =
          _calculateTimeDifference(_date2022Start);

      final time2014 =
          _calculateTimeDifference(_date2014Start);

      final imagePath = _imagePath;

      await HomeWidget.saveWidgetData(
        'text_2022',
        time2022,
      );

      await HomeWidget.saveWidgetData(
        'text_2014',
        time2014,
      );

      await HomeWidget.saveWidgetData(
        'show2022',
        _show2022,
      );

      await HomeWidget.saveWidgetData(
        'show2014',
        _show2014,
      );

      await HomeWidget.renderFlutterWidget(
        TimeOfWarWidgetRender(
          show2022: _show2022,
          show2014: _show2014,
          time2022: time2022,
          time2014: time2014,
          fontSize: _fontSize * 2.5,
          strokeWidth: _strokeWidth * 2.5,
          bgColor: _backgroundColor(),
          textColor: _textColor(),
          strokeColor: _strokeColor(),
          imagePath: imagePath,
        ),
        key: 'widget_rendered',
        logicalSize: const Size(400, 400),
      );

      await HomeWidget.updateWidget(
        name: 'WidgetProvider',
        androidName: 'WidgetProvider',
      );
    } catch (e) {
      debugPrint('HomeWidget Error: $e');
    }
  }

  String _calculateTimeDifference(DateTime startDate) {
    final now = DateTime.now();

    if (_showDaysOnly) {
      final difference = now.difference(startDate);

      final totalDays = difference.inDays + 1;

      var hours = now.hour - startDate.hour;

      if (hours < 0) {
        hours += 24;
      }

      var result = '${totalDays}д.';

      if (_showHour) {
        result += ' ${hours}г.';
      }

      return result;
    }

    var years = now.year - startDate.year;
    var months = now.month - startDate.month;
    var days = now.day - startDate.day;
    var hours = now.hour - startDate.hour;

    if (hours < 0) {
      days--;
      hours += 24;
    }

    if (days < 0) {
      months--;

      final previousMonth =
          DateTime(now.year, now.month, 0);

      days += previousMonth.day;
    }

    if (months < 0) {
      years--;
      months += 12;
    }

    days++;

    final daysInCurrentMonth =
        DateTime(now.year, now.month + 1, 0).day;

    if (days > daysInCurrentMonth) {
      days -= daysInCurrentMonth;
      months++;
    }

    if (months > 11) {
      months -= 12;
      years++;
    }

    var result =
        '${years}р. ${months}міс. ${days}д.';

    if (_showHour) {
      result += ' ${hours}г.';
    }

    return result;
  }

  Widget _buildOutlinedText(String text) {
    final textColor = _textColor();
    final strokeColor = _strokeColor();

    return Stack(
      children: [
        Text(
          text,
          style: TextStyle(
            fontSize: _fontSize,
            fontWeight: FontWeight.bold,
            foreground: Paint()
              ..style = PaintingStyle.stroke
              ..strokeWidth = _strokeWidth
              ..color = strokeColor,
          ),
        ),
        Text(
          text,
          style: TextStyle(
            fontSize: _fontSize,
            fontWeight: FontWeight.bold,
            color: textColor,
          ),
        ),
      ],
    );
  }

  Widget _buildWidgetPreview() {
    final bgColor = _backgroundColor();

    final hasImage =
        _imagePath != null &&
        _imagePath!.isNotEmpty &&
        File(_imagePath!).existsSync();

    return Padding(
      padding: const EdgeInsets.all(12),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Text(
            "Прев'ю віджета:",
            style: TextStyle(
              color: Colors.grey,
              fontSize: 12,
            ),
          ),
          const SizedBox(height: 6),
          Container(
            width: 400,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(28),
              color: hasImage ? null : bgColor,
              image: hasImage
                  ? DecorationImage(
                      image: FileImage(
                        File(_imagePath!),
                      ),
                      fit: BoxFit.cover,
                    )
                  : null,
              border: Border.all(
                color: Colors.white10,
                width: 1,
              ),
            ),
            child: Container(
              alignment: Alignment.center,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(28),
                color: hasImage ? bgColor : null,
              ),
              padding: const EdgeInsets.symmetric(
                vertical: 24,
                horizontal: 16,
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (_show2022) ...[
                    const Text(
                      'Повномасштабна війна:',
                      softWrap: false,
                      style: TextStyle(
                        color: Colors.white70,
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    _buildOutlinedText(
                      _calculateTimeDifference(
                        _date2022Start,
                      ),
                    ),
                  ],
                  if (_show2014) ...[
                    if (_show2022)
                      const SizedBox(height: 10),
                    const Text(
                      'Війна з 2014 року:',
                      softWrap: false,
                      style: TextStyle(
                        color: Colors.white70,
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    _buildOutlinedText(
                      _calculateTimeDifference(
                        _date2014Start,
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<String?> _cropImage(String path) async {
    final cropped = await ImageCropper().cropImage(
      sourcePath: path,
      aspectRatio: const CropAspectRatio(
        ratioX: 1,
        ratioY: 1,
      ),
      uiSettings: [
        AndroidUiSettings(
          toolbarTitle: 'Кадрування фону 1:1',
          toolbarColor: const Color(0xFF212121),
          toolbarWidgetColor: Colors.white,
          initAspectRatio:
              CropAspectRatioPreset.square,
          lockAspectRatio: true,
        ),
      ],
    );

    return cropped?.path;
  }

  Future<void> _pickImage() async {
    try {
      final picker = ImagePicker();

      final pickedFile = await picker.pickImage(
        source: ImageSource.gallery,
      );

      if (pickedFile == null) {
        return;
      }

      final cropped =
          await _cropImage(pickedFile.path);

      final finalPath =
          cropped ?? pickedFile.path;

      final savedPath = await HomeWidget.saveImage(
        'widget_background',
        FileImage(File(finalPath)),
      );

      if (savedPath.isEmpty) {
        debugPrint(
          'HomeWidget Error: background image path is empty',
        );
        return;
      }

      final prefs =
          await SharedPreferences.getInstance();

      await prefs.setString(
        'imagePath',
        savedPath,
      );

      if (!mounted) return;

      setState(() {
        _imagePath = savedPath;
      });

      await _updateHomeWidget();
    } catch (e) {
      debugPrint(
        'Background image error: $e',
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Час Війни'),
          elevation: 0,
          backgroundColor: Colors.transparent,
          bottom: const TabBar(
            indicatorColor:
                Colors.deepPurpleAccent,
            tabs: [
              Tab(text: 'Головне'),
              Tab(text: 'Кольори'),
              Tab(text: 'Контур'),
            ],
          ),
        ),
        body: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            _buildWidgetPreview(),
            Expanded(
              child: Container(
                decoration: const BoxDecoration(
                  color: Color(0xFF1E1E1E),
                  borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(24),
                    topRight: Radius.circular(24),
                  ),
                ),
                child: TabBarView(
                  children: [
                    _buildMainTab(),
                    _buildColorsTab(),
                    _buildContourTab(),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMainTab() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        SwitchListTile(
          title: const Text('Війна 2022'),
          value: _show2022,
          activeColor: Colors.deepPurpleAccent,
          onChanged: (value) {
            setState(() {
              _show2022 = value;
            });

            _saveSetting(
              'show2022',
              value,
            );
          },
        ),
        SwitchListTile(
          title: const Text(
            'Війна 2014',
            softWrap: false,
          ),
          value: _show2014,
          activeColor: Colors.deepPurpleAccent,
          onChanged: (value) {
            setState(() {
              _show2014 = value;
            });

            _saveSetting(
              'show2014',
              value,
            );
          },
        ),
        SwitchListTile(
          title: const Text('Показ годин'),
          value: _showHour,
          activeColor: Colors.deepPurpleAccent,
          onChanged: (value) {
            setState(() {
              _showHour = value;
            });

            _saveSetting(
              'showHour',
              value,
            );
          },
        ),
        SwitchListTile(
          title: const Text('Облік в днях'),
          value: _showDaysOnly,
          activeColor: Colors.deepPurpleAccent,
          onChanged: (value) {
            setState(() {
              _showDaysOnly = value;
            });

            _saveSetting(
              'showDaysOnly',
              value,
            );
          },
        ),
        const Divider(
          color: Colors.white10,
        ),
        const Text(
          'Прозорість фону',
          style: TextStyle(
            fontSize: 14,
          ),
        ),
        Slider(
          value: _opacity,
          min: 0,
          max: 1,
          activeColor: Colors.deepPurpleAccent,
          onChanged: (value) {
            setState(() {
              _opacity = value;
            });

            _saveSetting(
              'opacity',
              value,
            );
          },
        ),
        const Text(
          'Розмір тексту',
          style: TextStyle(
            fontSize: 14,
          ),
        ),
        Slider(
          value: _fontSize,
          min: 12,
          max: 40,
          activeColor: Colors.deepPurpleAccent,
          onChanged: (value) {
            setState(() {
              _fontSize = value;
            });

            _saveSetting(
              'fontSize',
              value,
            );
          },
        ),
        const SizedBox(height: 10),
        ElevatedButton.icon(
          onPressed: _pickImage,
          icon: const Icon(Icons.image),
          label: const Text('Вибрати ФОТО'),
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFF2D2D2D),
            padding: const EdgeInsets.symmetric(
              vertical: 14,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildColorsTab() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        const Text(
          'Колір тексту',
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.deepPurpleAccent,
          ),
        ),
        const SizedBox(height: 8),
        _buildRGBSliders(
          _tr,
          _tg,
          _tb,
          (r, g, b) {
            setState(() {
              _tr = r;
              _tg = g;
              _tb = b;
            });

            _saveSetting('tr', r);
            _saveSetting('tg', g);
            _saveSetting('tb', b);
          },
        ),
        const Divider(
          color: Colors.white10,
          height: 32,
        ),
        const Text(
          'Колір фону',
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.deepPurpleAccent,
          ),
        ),
        const SizedBox(height: 8),
        _buildRGBSliders(
          _br,
          _bg,
          _bb,
          (r, g, b) {
            setState(() {
              _br = r;
              _bg = g;
              _bb = b;
            });

            _saveSetting('br', r);
            _saveSetting('bg', g);
            _saveSetting('bb', b);
          },
        ),
      ],
    );
  }

  Widget _buildContourTab() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        const Text(
          'Колір контуру',
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.deepPurpleAccent,
          ),
        ),
        const SizedBox(height: 8),
        _buildRGBSliders(
          _sr,
          _sg,
          _sb,
          (r, g, b) {
            setState(() {
              _sr = r;
              _sg = g;
              _sb = b;
            });

            _saveSetting('sr', r);
            _saveSetting('sg', g);
            _saveSetting('sb', b);
          },
        ),
        const Divider(
          color: Colors.white10,
          height: 32,
        ),
        const Text(
          'Товщина контуру',
          style: TextStyle(
            fontSize: 14,
          ),
        ),
        Slider(
          value: _strokeWidth,
          min: 0,
          max: 8,
          activeColor: Colors.deepPurpleAccent,
          onChanged: (value) {
            setState(() {
              _strokeWidth = value;
            });

            _saveSetting(
              'strokeWidth',
              value,
            );
          },
        ),
      ],
    );
  }

  Widget _buildRGBSliders(
    double r,
    double g,
    double b,
    void Function(
      double r,
      double g,
      double b,
    ) onChanged,
  ) {
    return Column(
      children: [
        Row(
          children: [
            const SizedBox(
              width: 20,
              child: Text(
                'R',
                style: TextStyle(
                  color: Colors.red,
                ),
              ),
            ),
            Expanded(
              child: Slider(
                value: r,
                min: 0,
                max: 255,
                activeColor: Colors.red,
                onChanged: (value) {
                  onChanged(
                    value,
                    g,
                    b,
                  );
                },
              ),
            ),
          ],
        ),
        Row(
          children: [
            const SizedBox(
              width: 20,
              child: Text(
                'G',
                style: TextStyle(
                  color: Colors.green,
                ),
              ),
            ),
            Expanded(
              child: Slider(
                value: g,
                min: 0,
                max: 255,
                activeColor: Colors.green,
                onChanged: (value) {
                  onChanged(
                    r,
                    value,
                    b,
                  );
                },
              ),
            ),
          ],
        ),
        Row(
          children: [
            const SizedBox(
              width: 20,
              child: Text(
                'B',
                style: TextStyle(
                  color: Colors.blue,
                ),
              ),
            ),
            Expanded(
              child: Slider(
                value: b,
                min: 0,
                max: 255,
                activeColor: Colors.blue,
                onChanged: (value) {
                  onChanged(
                    r,
                    g,
                    value,
                  );
                },
              ),
            ),
          ],
        ),
      ],
    );
  }
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}
