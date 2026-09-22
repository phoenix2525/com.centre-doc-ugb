@echo off
chcp 65001 > nul
echo ==============================================================================
echo  VALIDATION DES REGLES METIER ACADEMIQUES (POO2 - UGB)
echo ==============================================================================

if not exist centre_doc.jar (
    call compile.bat
)

java -Dfile.encoding=UTF-8 -cp "centre_doc.jar;lib/mysql-connector-j-9.0.0.jar" sn.ugb.centredoc.TestReglesMetier
echo.
pause
