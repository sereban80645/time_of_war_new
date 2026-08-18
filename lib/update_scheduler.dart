import 'dart:async';

class UpdateScheduler {
  Timer? _timer;

  void start(Future<void> Function() onUpdate) {
    _timer?.cancel();
    _scheduleNext(onUpdate);
  }

  void _scheduleNext(Future<void> Function() onUpdate) {
    final now = DateTime.now();

    var next = DateTime(
      now.year,
      now.month,
      now.day,
      now.hour + 1,
      1,
    );

    if (!next.isAfter(now)) {
      next = next.add(
        const Duration(hours: 1),
      );
    }

    _timer = Timer(
      next.difference(now),
      () async {
        try {
          await onUpdate();
        } finally {
          _scheduleNext(onUpdate);
        }
      },
    );
  }

  void dispose() {
    _timer?.cancel();
    _timer = null;
  }
}
