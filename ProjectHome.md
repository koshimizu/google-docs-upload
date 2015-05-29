# Google Docs Batch Upload #

A tool for batch uploading documents to a Google Docs account with recursive directory traversing. The tool **supports PDF upload**.

Please note, uploading any types of files without conversion is available only for Google Apps for Business accounts due to API restrictions.

To run the application you need Java 1.6 (JRE 6) or greater installed: http://java.sun.com/javase/6/

**IMPORTANT:** if you get the following error: "Uploading without conversion is only available to Google Apps for Business accounts", please try adding "--protocol https" to the end of your command. This may help to solve the problem.



```
 Usage: java -jar google-docs-upload.jar
 Usage: java -jar google-docs-upload.jar <path> --recursive
 Usage: java -jar google-docs-upload.jar <path> --username <username> --password <password>
 Usage: java -jar google-docs-upload.jar <path> --auth-sub <token>
     [--username <username>]       Username for a Google account.
     [--password <password>]       Password for a Google account.
     [--recursive]                 Recursively upload all subfolders.
     [--remote-folder <path>]      The remote folder path to upload the documents separated by '/'.
     [--without-conversion]        Do not convert documents into the Google Docs format (unsupported files are not converted by default).
     [--without-folders]           Do not recreate folder structure in Google Docs.
     [--hide-all]                  Hide all documents after uploading.
     [--add-all]                   Upload all documents even if there are already documents with the same names.		
     [--skip-all]                  Skip all documents if there there are already documents with the same names.	
     [--replace-all]               Replace all documents in Google Docs, which have the same names as the uploaded.	
     [--disable-retries]           Disable auto-retries in the cases of failed upload.	
     [--auth-sub <token>]          AuthSub token.
     [--auth-protocol <protocol>]  The protocol to use with authentication.
     [--auth-host <host:port>]     The host of the auth server to use.
     [--protocol <protocol>]       The protocol to use with the HTTP requests.
     [--host <host:port>]          Where is the feed (default = docs.google.com)
```



You can also use short versions of the options, such as -u (--username), -p (--password), -rf (--remote-folder), etc.


<a href='https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=11163748'><img src='https://www.paypal.com/en_AU/i/btn/btn_donateCC_LG.gif' /></a>



### Setup for easy launch in Windows ###

1. Create a new folder, let's say _C:\gdu\_

2. Download the latest jar file and copy it into the created folder

3. Create in _C:\gdu\_ an empty file called _gdu.cmd_

4. Open the file _gdu.cmd_ (for example, using Notepad) and paste into it the following content:

```
java -jar C:\gdu\google-docs-upload-<version>.jar %1 --recursive -u <username> %*
pause
```

5. Replace `<version>` with the actual version of the tool and `<username>` with your Google account username

6. Run the program by double-clicking on _gdu.cmd_

7. (Optional) Add the install path (_C:\gdu\_) to the [Windows PATH variable](http://en.wikipedia.org/wiki/Environment_variable#System_path_variables)

8. (Optional) Now you can run the tool from Windows command line: `gdu <path> [<options>]`

Let's say you have a folder _C:\documents\_ with documents that you often update with new ones. In order to simplify the regular upload of new documents to Google Docs, you can do the following (please note, to make these steps work, first you have to add the install path _C:\gdu\_ to the PATH variable):

1. Create in _C:\Documents\_ an empty file _upload.cmd_

2. Copy into _C:\Documents\upload.cmd_ the content:

```
gdu.cmd ./ -rf /Documents -r -sa
```

3. To run the upload you just need to double-click on _upload.cmd_ and supply your username and password

4. Or you can schedule regular auto execution of the script using, for example, [Windows Task Scheduler](http://en.wikipedia.org/wiki/Task_Scheduler)

### Proxy support ###

To enable proxy support, run the program with the following standard Java properties specified:

```
java -DProxyHost=<host> -DProxyPort=<port> -jar google-docs-upload.jar
```

### Thanks for the donations! ###

Please let me know if you want to remove your name from this list or add some information.

  * John Thompson
  * David Durant
  * Duane Penzien
  * Eike Benedikt Lotz
  * OnL9, Inc.
  * Tracy Martin
  * John Murphy
  * Robert Clay
  * Clint Larrabee

### Changelog ###

> _Version 1.4.7 (24/07/2011)_
    * Fixed a bug causing changes of folder permissions

> _Version 1.4.6 (12/04/2011)_
    * Fixed duplicate detection for files uploaded without conversion

> _Version 1.4.5 (07/04/2011)_
    * Fixed a bug: Fixed a bug: another problem with mime types, this time with convertable documents

> _Version 1.4.4 (06/04/2011)_
    * Fixed a bug: some files could not be uploaded due to wrong mime types

> _Version 1.4.3 (21/03/2011)_
    * The tool has been fixed and tested for working properly with Google Apps for Business accounts. _Thanks to http://digiprove.com/ for providing a Business account for testing purposes_
    * Fixed a bug caused by empty file names
    * The dependencies have been updated to new versions

> _Version 1.4.2 (18/01/2011)_
    * Bug fixes

> _Version 1.4.1 (29/12/2010)_
    * Fixed a bug with uploading without conversion

> _Version 1.4 (23/12/2010)_
    * Added the --without-conversion option for support of uploading files of any types for Business accounts. _Thanks to Sam Cooley for contributing a patch_
    * Added the --hide-all option for auto-hiding files after uploading
    * Added output of document IDs
    * Removed the enforcement of the size limitations
    * Documents are now replaced properly with keeping the same resource ID and without loosing the metadata, such as sharing settings
    * Bug fixes

> _Version 1.3.2 (01/03/2010)_
    * Fixed a bug: NullPointerException when uploading documents without specification of the remote folder

> _Version 1.3.1 (02/01/2010)_
    * Fixed skip-all option for folders with many documents
    * Fixed handling of non-alphabetical symbols in folder names
    * Fixed the file counter in the progress indicator

> _Version 1.3 (27/12/2009)_
    * Google Docs size limits are enforced
    * Added an option to specify the remote folder to upload documents
    * Added an option to disable auto-retries
    * Fixed the bug in uploading a single file
    * Added the upload progress indicator

> _Version 1.2 (06/09/2009)_
    * Added the ability to replicate the folder structure in Google Docs

> _Version 1.1 (03/09/2009)_
    * Bug fixes
