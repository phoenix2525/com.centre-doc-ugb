@echo off
chcp 65001 > nul
echo ==============================================================================
echo  LANCEMENT DU SYSTEME D'INFORMATION DES CENTRES DE DOCUMENTATION UGB
echo ==============================================================================

if not exist centre_doc.jar (
    echo Compilation requise...
    call compile.bat
)

echo Demarrage de l'application graphique Swing...
start javaw -Dfile.encoding=UTF-8 -cp "centre_doc.jar;lib/mysql-connector-j-9.0.0.jar" sn.ugb.centredoc.Main
exit
