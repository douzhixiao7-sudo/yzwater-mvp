@echo off
rem Local Node.js toolchain for this development computer.
set "PATH=C:\Users\Administrator\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin;C:\Users\Administrator\.cache\codex-runtimes\codex-primary-runtime\dependencies\bin\fallback;%PATH%"
node --version
pnpm --version
echo Node.js environment is ready for this command window.
