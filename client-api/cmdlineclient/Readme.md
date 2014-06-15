### CLC Java

Operations in this project assume that mono, java and postgres are installed.

Eclipse syntax highlighting.
   
Atomic actions:

1. parse
2. diff
3. unmanaged sources
4. migration sql

general use case:

deploy (each step considers previous successful):
 - displays diff
 - parse result
 - if not skip-diff prompt user to continue with this changes
 - migration information
 - if the migration performs a destructive operation user will be informed and prompted to continue
 - apply migration
 - generate sources
 - compile sources
 - deploy assembly

After a successful operation user will will be informed to restart the mono service.  
