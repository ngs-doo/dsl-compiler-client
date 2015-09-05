.386
.model flat,stdcall
option casemap:none

include \masm32\include\windows.inc
include \masm32\include\kernel32.inc
includelib \masm32\lib\kernel32.lib

.code
    output db "Usage: jar", 13, 10

start:
    invoke GetStdHandle, STD_OUTPUT_HANDLE

    push esp
    mov ecx, esp
    invoke WriteFile, eax, addr output, sizeof output, ecx, NULL
    pop eax

    invoke ExitProcess, 0
end start
