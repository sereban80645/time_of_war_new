import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:home_widget/home_widget.dart';
class SettingsPage extends StatefulWidget {
const SettingsPage({Key? key}) : super(key: key);
@override
State<SettingsPage> createState() => _SettingsPageState();
}
class _SettingsPageState extends State<SettingsPage> {
final ValueNotifier<bool> _showOrdinalDay = ValueNotifier<bool>(true);
final ValueNotifier<String> _startDate = ValueNotifier<String>('24 лютого 2022 05:00');
final ValueNotifier<String> _theme = ValueNotifier<String>('Військова');
@override
void initState() {
super.initState();
_loadSettings();
}
Future<void> _loadSettings() async {
final prefs = await SharedPreferences.getInstance();
_showOrdinalDay.value = prefs.getBool('show_ordinal_day') ?? true;
_startDate.value = prefs.getString('start_date') ?? '24 лютого 2022 05:00';
_theme.value = prefs.getString('theme') ?? 'Військова';
}
Future<void> _updateSetting(String key, dynamic value) async {
final prefs = await SharedPreferences.getInstance();
if (value is bool) {
await prefs.setBool(key, value);
await HomeWidget.saveWidgetData<bool>(key, value);
} else if (value is String) {
await prefs.setString(key, value);
await HomeWidget.saveWidgetData<String>(key, value);
}
await HomeWidget.updateWidget(name: 'AppWidgetProvider', androidName: 'AppWidgetProvider');
}
@override
Widget build(BuildContext context) {
return Scaffold(
appBar: AppBar(title: const Text('Налаштування Time_of_waR'), backgroundColor: const Color(0xFF1A1A1A)),
body: Container(
color: const Color(0xFF121212),
padding: const EdgeInsets.all(16.0),
child: ListView(
children: [
ValueListenableBuilder<bool>(
valueListenable: _showOrdinalDay,
builder: (context, isOrdinal, child) {
return SwitchListTile(
title: const Text('Формат відліку', style: TextStyle(color: Colors.white)),
subtitle: Text(isOrdinal ? 'Поточний день події' : 'Загальний минулий час', style: const TextStyle(color: Colors.grey)),
value: isOrdinal,
activeColor: const Color(0xFF2196F3),
onChanged: (value) { _showOrdinalDay.value = value; _updateSetting('show_ordinal_day', value); },
);
},
),
const Divider(color: Colors.grey),
ValueListenableBuilder<String>(
valueListenable: _startDate,
builder: (context, date, child) {
return ListTile(
title: const Text('Точка відліку', style: TextStyle(color: Colors.white)),
trailing: DropdownButton<String>(
value: date,
dropdownColor: const Color(0xFF1A1A1A),
style: const TextStyle(color: Colors.white),
items: ['20 лютого 2014', '24 лютого 2022 05:00'].map((d) { return DropdownMenuItem(value: d, child: Text(d)); }).toList(),
onChanged: (val) { if (val != null) { _startDate.value = val; _updateSetting('start_date', val); } },
),
);
},
),
const Divider(color: Colors.grey),
ValueListenableBuilder<String>(
valueListenable: _theme,
builder: (context, theme, child) {
return ListTile(
title: const Text('Стиль відображення', style: TextStyle(color: Colors.white)),
trailing: DropdownButton<String>(
value: theme,
dropdownColor: const Color(0xFF1A1A1A),
style: const TextStyle(color: Colors.white),
items: ['Світла', 'Темна', 'Військова'].map((t) { return DropdownMenuItem(value: t, child: Text(t)); }).toList(),
onChanged: (val) { if (val != null) { _theme.value = val; _updateSetting('theme', val); } },
),
);
},
),
],
),
),
);
}
}
