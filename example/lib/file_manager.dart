import 'dart:io';

import 'package:path_provider/path_provider.dart';

class FileManager {
  static Future<void> writeToLogFile(String log) async {
    final file = await _getTempLogFile();
    await file.writeAsString(log, mode: FileMode.append);
  }

  static Future<String> readLogFile() async {
    final file = await _getTempLogFile();
    return file.readAsString();
  }

  static Future<File> _getTempLogFile() async {
    final directory = await getTemporaryDirectory();
    return File('${directory.path}/log.txt');
  }

  static Future<void> clearLogFile() async {
    final file = await _getTempLogFile();
    await file.writeAsString('');
  }
}
