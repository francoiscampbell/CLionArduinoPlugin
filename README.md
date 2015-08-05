#CLion Arduino Plugin

This is a JetBrains CLion plugin that integrates [Arduino CMake](https://github.com/queezythegreat/arduino-cmake) into the IDE.

The current features are to convert a default CLion project into an Arduino-CMake one, and to create new sketch files. When CLion adds the ability to hook into project creation directly, I will be able to make it selectable, but for now, we're stuck with having to create a new CLion project and then convert it.

Future features are to add menu option to easily change the board type, port number, etc.

**Version 1.0**

*   Convert a project to Arduino CMake. This replaces CMakeLists.txt with a default one, deletes the default main.cpp file, copies in the Arduino CMake toolchain files, and deletes the build direcory to start fresh
*   Associates .ino and .pde files as C++ source, so you get syntax highlighting and prediction, etc.
*   Create a new sketch file in any directory. If you omit the extension, it will add .ino automatically
*   Adds import for Arduino.h to all newly created sketch files to enable code completion