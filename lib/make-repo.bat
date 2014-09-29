call mvn deploy:deploy-file -Durl=file:../repo -Dfile=jansi-1.11.jar -DgroupId=org.fusesource.jansi -DartifactId=jansi -Dversion=1.11 -Dpackaging=jar
call mvn deploy:deploy-file -Durl=file:../repo -Dfile=postgresql-9.3-1102.jdbc4.jar -DgroupId=org.postgresql -DartifactId=postgresql -Dversion=9.3-1102-jdbc4 -Dpackaging=jar
