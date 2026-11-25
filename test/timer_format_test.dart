import 'package:flutter_test/flutter_test.dart';
import 'package:time_of_war/main.dart';

void main() {
  group('formatElapsedTime — day-count mode', () {
    final start = DateTime.utc(2022, 2, 24, 2, 40);

    test('counts the event day only after its local anniversary time', () {
      expect(
        formatElapsedTime(start, DateTime.utc(2026, 10, 31, 2, 39), true, true),
        '1710д. 23г.',
      );
      expect(
        formatElapsedTime(start, DateTime.utc(2026, 10, 31, 2, 40), true, true),
        '1711д. 0г.',
      );
    });

    test('hiding hours changes only the text, not the day count', () {
      final now = DateTime.utc(2026, 10, 31, 17, 40);
      expect(formatElapsedTime(start, now, true, true), '1711д. 15г.');
      expect(formatElapsedTime(start, now, true, false), '1711д.');
    });

    test('uses calendar-date differences rather than elapsed 24-hour blocks', () {
      final dstStart = DateTime(2026, 3, 28, 2, 40);
      final afterDst = DateTime(2026, 3, 29, 2, 40);
      expect(formatElapsedTime(dstStart, afterDst, true, true), '2д. 0г.');
    });
  });

  group('formatElapsedTime — calendar mode', () {
    final start2022 = DateTime.utc(2022, 2, 24, 2, 40);
    final now = DateTime.utc(2026, 10, 31, 17, 40);

    test('shows years, months, days and hours with abbreviated month unit', () {
      expect(
        formatElapsedTime(start2022, now, false, true),
        '4р. 8м. 7д. 15г.',
      );
    });

    test('hiding hours preserves the calendar date components', () {
      expect(
        formatElapsedTime(start2022, now, false, false),
        '4р. 8м. 7д.',
      );
    });

    test('2014 event uses the same formatter and month abbreviation', () {
      expect(
        formatElapsedTime(
          DateTime.utc(2014, 2, 20, 12),
          now,
          false,
          true,
        ),
        '12р. 8м. 11д. 5г.',
      );
    });
  });
  group('nextBackgroundUpdateTime', () {
    test('uses :40 for 2022 and :00 for 2014, selecting the next event', () {
      expect(nextBackgroundUpdateTime(DateTime(2026, 11, 25, 19, 39), true, true, true), DateTime(2026, 11, 25, 19, 40));
      expect(nextBackgroundUpdateTime(DateTime(2026, 11, 25, 19, 41), true, true, true), DateTime(2026, 11, 25, 20, 0));
    });
    test('uses event start times for daily updates when hours are hidden', () {
      expect(nextBackgroundUpdateTime(DateTime(2026, 11, 25, 19, 41), true, true, false), DateTime(2026, 11, 26, 2, 40));
      expect(nextBackgroundUpdateTime(DateTime(2026, 11, 25, 11, 0), false, true, false), DateTime(2026, 11, 25, 12, 0));
    });
    test('returns null when both counters are disabled', () {
      expect(nextBackgroundUpdateTime(DateTime(2026, 11, 25), false, false, true), isNull);
    });
  });

  test('2014 counter changes from 6 to 7 hours exactly at 19:00', () {
    final start = DateTime(2014, 2, 20, 12, 0);
    expect(formatElapsedTime(start, DateTime(2026, 11, 25, 18, 59), false, true), '12р. 9м. 5д. 6г.');
    expect(formatElapsedTime(start, DateTime(2026, 11, 25, 19, 0), false, true), '12р. 9м. 5д. 7г.');
  });

  group('scheduleAlarmWithFallback', () {
    test('falls back when exact alarm returns false', () async {
      final attempts = <bool>[];
      final result = await scheduleAlarmWithFallback((exact) async { attempts.add(exact); return !exact; });
      expect(result, isTrue);
      expect(attempts, [true, false]);
    });
    test('does not request inexact alarm when exact succeeds', () async {
      final attempts = <bool>[];
      final result = await scheduleAlarmWithFallback((exact) async { attempts.add(exact); return true; });
      expect(result, isTrue);
      expect(attempts, [true]);
    });
    test('falls back after an exact alarm exception and reports total failure', () async {
      final attempts = <bool>[];
      final result = await scheduleAlarmWithFallback((exact) async {
        attempts.add(exact);
        if (exact) throw StateError('exact alarms unavailable');
        return false;
      });
      expect(result, isFalse);
      expect(attempts, [true, false]);
    });
  });
}
