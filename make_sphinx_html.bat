@echo off

REM Sphinx�̃h�L�������g���r���h���Atareget��site�t�H���_�ɔz�u����B

echo "Building sphinx documentation for version %1"

%~d0
cd %~p0

pushd .\src\site\sphinx

rmdir /q /s build
call make html PACKAGE_VERSION=%1

popd

rmdir /q /s .\target\site\sphinx
mkdir .\target\site\sphinx
xcopy /y /e .\src\site\sphinx\build\html .\target\site\sphinx

REM github-pages��sphinx�Ή�
echo "" > .\target\site\.nojekyll

