#!bin/bash
fileNames=( InspectManagedProjectChanges GetLastManagedDSL GetConfig UpdateManagedProject GenerateMigrationSQL GenerateSources GenerateUnmanagedSources GetProjectByName GetAllProjects RenameProject CleanProject TemplateGet TemplateCreate TemplateListAll TemplateDelete)

dir="$( dirname "$(readlink -f "$0")" )"

for fileName in "${fileNames[@]}"
do
  cp mock/processor/DownloadGeneratedModelProcessor.java  $dir/mock/processor/${fileName}Processor.java
  sed -i "s/DownloadGeneratedModel/$(echo $fileName)/g" $dir/mock/processor/${fileName}Processor.java
  cp test/transport/CreateProjectTransportTest.java  $dir/test/transport/${fileName}TransportTest.java
  sed -i "s/CreateProject/$(echo $fileName)/g" $dir/test/transport/${fileName}TransportTest.java
  camel=$(echo $fileName | sed -r 's/(\w)(\w+)/\l\1\2/g')
  sed -i "s/createTestProject/$(echo $camel)/g" $dir/test/transport/${fileName}TransportTest.java
  echo copied            $fileName
done
