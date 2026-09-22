@echo off
chcp 65001 > nul
echo ==============================================================================
echo  COMPILATION DU PROJET POO2 - SI CENTRES DE DOCUMENTATION UGB
echo ==============================================================================

if not exist bin mkdir bin

echo [1/2] Compilation des sources Java...
javac -encoding UTF-8 -cp "lib/mysql-connector-j-9.0.0.jar;src" -d bin @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] La compilation a echoue. Verifiez les erreurs ci-dessus.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Creation de l'archive JAR executable 'centre_doc.jar'...
jar cfe centre_doc.jar sn.ugb.centredoc.Main -C bin .

echo ==============================================================================
echo  SUCCES : Le projet est compile et l'archive 'centre_doc.jar' est prete !
echo ==============================================================================
