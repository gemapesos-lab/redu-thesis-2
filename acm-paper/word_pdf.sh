#!/bin/bash
# Export a docx to PDF through Microsoft Word, using Word's own container dir
# (avoids the macOS "Grant File Access" prompt).
set -e
C="$HOME/Library/Containers/com.microsoft.Word/Data/tmp"
mkdir -p "$C"
cp "$1" "$C/in.docx"; rm -f "$C/out.pdf"
osascript <<OSA
tell application "Microsoft Word"
  set theDoc to open file name POSIX file "$C/in.docx" with read only
  save as theDoc file name POSIX file "$C/out.pdf" file format format PDF
  close theDoc saving no
end tell
OSA
cp "$C/out.pdf" "$2"
