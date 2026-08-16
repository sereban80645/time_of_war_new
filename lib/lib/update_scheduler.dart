import 'package:workmanager/workmanager.dart';

const String updateTaskName = 'updateWidgetTask';
const String updateTaskUniqueName = 'time_of_war_update';

Duration durationUntil(DateTime target) {
  final now = DateTime.now();

  if (target.isBefore(now)) {
    target = target.add(const Duration(days: 1));
  }

  return target.difference(now);
}

DateTime nextHourlyUpdate() {
  final now = DateTime.now();

  return DateTime(
    now.year,
    now.month,
    now.day,
    now.hour + 1,
    1,
  );
}

DateTime nextDailyUpdate(int hour) {
  final now = DateTime.now();

  var target = DateTime(
    now.year,
    now.month,
    now.day,
    hour,
    1,
  );

  if (!target.isAfter(now)) {
    target = target.add(const Duration(days: 1));
  }

  return target;
}

Future<void> scheduleNextHourlyUpdate() async {
  await Workmanager().registerOneOffTask(
    updateTaskUniqueName,
    updateTaskName,
    initialDelay: durationUntil(nextHourlyUpdate()),
    existingWorkPolicy: ExistingWorkPolicy.replace,
  );
}

Future<void> scheduleNextDailyUpdate(int hour) async {
  final uniqueName = 'time_of_war_daily_$hour';

  await Workmanager().registerOneOffTask(
    uniqueName,
    updateTaskName,
    initialDelay: durationUntil(nextDailyUpdate(hour)),
    existingWorkPolicy: ExistingWorkPolicy.replace,
  );
}

Future<void> scheduleAllUpdates() async {
  await scheduleNextHourlyUpdate();
  await scheduleNextDailyUpdate(5);
  await scheduleNextDailyUpdate(12);
}
