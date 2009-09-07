/* Copyright (c) 2009 Anton Beloglazov, http://beloglazov.info
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.InvalidEntryException;
import com.google.gdata.util.ServiceException;

/**
 * Google Docs Upload
 * 
 * A tool for batch upload of documents to a Google Docs account preserving folder structure.
 * Supported file formats are: csv, doc, docx, html, htm, ods, odt, pdf, ppt, pps, rtf, sxw, tsv, tab, txt, xls, xlsx.
 * 
 * Usage: java -jar google-docs-upload.jar
 * Usage: java -jar google-docs-upload.jar <path> --recursive
 * Usage: java -jar google-docs-upload.jar <path> --username <username> --password <password>
 * Usage: java -jar google-docs-upload.jar <path> --authSub <token>
 * [--username <username>]       Username for a Google account.
 * [--password <password>]       Password for a Google account.
 * [--recursive]                 Recursively upload all subfolders.
 * [--without-folders]           Do not recreate folder structure in Google Docs.
 * [--add-all]                   Upload all documents even if there are already documents with the same names.		
 * [--skip-all]                  Skip all documents if there there are already documents with the same names.		
 * [--replace-all]               Replace all documents in Google Docs, which have the same names as the uploaded.
 * [--authSub <token>]           AuthSub token.
 * [--auth_protocol <protocol>]  The protocol to use with authentication.
 * [--auth_host <host:port>]     The host of the auth server to use.
 * [--protocol <protocol>]       The protocol to use with the HTTP requests.
 * [--host <host:port>]          Where is the feed (default = docs.google.com)
 * 
 * @author Anton Beloglazov
 * @since 09/2009
 * @version 1.2
 */
public class GoogleDocsUpload {

	/** The document list. */
	private DocumentList documentList;
	
	/** The output stream *. */
	private static PrintWriter out;
	
	/** Supported file formats http://code.google.com/intl/ru/apis/documents/faq.html#WhatKindOfFilesCanIUpload * */
	public static String[] SUPPORTED_FORMATS = { "csv", "doc", "docx", "html", "htm", "ods", "odt", "pdf", "ppt", "pps", "rtf", "sxw", "tsv", "tab", "txt", "xls", "xlsx" };
	
	/** Welcome message, introducing the program. */
	private static final String[] WELCOME_MESSAGE = { "",
		"Using this tool, you can batch upload your documents to a Google Docs account preserving folder structure.",
		"Supported file formats are: csv, doc, docx, html, htm, ods, odt, pdf, ppt, pps, rtf, sxw, tsv, tab, txt, xls, xlsx.",
		"Type 'help' for a list of parameters.", "" 
	};	
	
	/** The message for displaying the usage parameters. */
	private static final String[] USAGE_MESSAGE = { "",
		"Usage: java -jar google-docs-upload.jar",
		"Usage: java -jar google-docs-upload.jar <path> --recursive",
		"Usage: java -jar google-docs-upload.jar <path> --username <username> --password <password>",
		"Usage: java -jar google-docs-upload.jar <path> --authSub <token>",
		"    [--username <username>]       Username for a Google account.",
		"    [--password <password>]       Password for a Google account.",
		"    [--recursive]                 Recursively upload all subfolders.",
		"    [--without-folders]           Do not recreate folder structure in Google Docs.",		
		"    [--add-all]                   Upload all documents even if there are already documents with the same names.",		
		"    [--skip-all]                  Skip all documents if there there are already documents with the same names.",		
		"    [--replace-all]               Replace all documents in Google Docs, which have the same names as the uploaded.",		
		"    [--authSub <token>]           AuthSub token.",
		"    [--auth_protocol <protocol>]  The protocol to use with authentication.",
		"    [--auth_host <host:port>]     The host of the auth server to use.",
		"    [--protocol <protocol>]       The protocol to use with the HTTP requests.",
		"    [--host <host:port>]          Where is the feed (default = docs.google.com)", "" 
	};

	/**
	 * Constructor.
	 * 
	 * @param appName the app name
	 * @param authProtocol the auth protocol
	 * @param authHost the auth host
	 * @param protocol the protocol
	 * @param host the host
	 * 
	 * @throws DocumentListException the document list exception
	 */
	public GoogleDocsUpload(String appName, String authProtocol, String authHost, String protocol, String host) throws DocumentListException {
		setDocumentList(new DocumentList(appName, authProtocol, authHost, protocol, host));
	}
	
	/**
	 * Runs the application.
	 * 
	 * @param args the command-line arguments
	 * 
	 * @throws DocumentListException the document list exception
	 * @throws ServiceException the service exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws DocumentListException, IOException, ServiceException {
		SimpleCommandLineParser parser = new SimpleCommandLineParser(args);
		String authProtocol = parser.getValue("auth_protocol");
		String authHost = parser.getValue("auth_host");
		String authSub = parser.getValue("authSub", "auth", "a");
		String username = parser.getValue("username", "user", "u");
		String password = parser.getValue("password", "pass", "p");
		String protocol = parser.getValue("protocol");
		String host = parser.getValue("host", "s");
		boolean help = parser.containsKey("help", "h");
		boolean recursive = parser.containsKey("recursive", "r");
		boolean withoutFolders = parser.containsKey("without-folders", "wf");
		boolean addAll = parser.containsKey("add-all", "aa");
		boolean skipAll = parser.containsKey("skip-all", "sa");
		boolean replaceAll = parser.containsKey("replace-all", "ra");
		String path = null;
		
		if (help) {
			printMessages(USAGE_MESSAGE);
			print("Supported file formats are: ");
			boolean notFirst = false;
			for (String format : SUPPORTED_FORMATS) {
				if (notFirst) {
					print(", ");	
				}
				print(format);				
				notFirst = true;
			}
			printLine("");
			System.exit(1);
		}
		
		printMessages(WELCOME_MESSAGE);

		if (args.length > 0 && args[0] != "help" && !args[0].substring(0, 1).equals("-") && !args[0].substring(0, 2).equals("--")) {
			path = args[0]; 			
		}

		if (authProtocol == null) {
			authProtocol = DocumentList.DEFAULT_AUTH_PROTOCOL;
		}

		if (authHost == null) {
			authHost = DocumentList.DEFAULT_AUTH_HOST;
		}

		if (protocol == null) {
			protocol = DocumentList.DEFAULT_PROTOCOL;
		}

		if (host == null) {
			host = DocumentList.DEFAULT_HOST;
		}
		
		Scanner scanner = null;
		
		if (path == null || ((username == null || password == null) && authSub == null)) {
			scanner = new Scanner(System.in);
			
			if (username == null && authSub == null) {
				print("Username: ");
				username = scanner.nextLine();						
			}
			
			if (password == null && authSub == null) {
				print("Password: ");
				password = String.copyValueOf(System.console().readPassword());
			}
		}
		
		GoogleDocsUpload app = new GoogleDocsUpload("google-docs-upload", authProtocol, authHost, protocol, host);

		if (password != null) {
			try {
				app.login(username, password);
			} catch (AuthenticationException e) {
				printLine("Authentification error");
				System.exit(1);
			}
		} else {
			try {
				app.login(authSub);
			} catch (AuthenticationException e) {
				printLine("Authentification error");
				System.exit(1);
			}
		}
		
		if (path == null) {
			if (scanner == null) {
				scanner = new Scanner(System.in);
			}
			
			print("Path: ");
			path = scanner.nextLine();						
		}

		app.upload(path, recursive, withoutFolders, addAll, skipAll, replaceAll);
	}

	/**
	 * Authenticates the client using ClientLogin.
	 * 
	 * @param username User's username
	 * @param password User's password
	 * 
	 * @throws DocumentListException the document list exception
	 * @throws AuthenticationException the authentication exception
	 */
	public void login(String username, String password) throws AuthenticationException, DocumentListException {
		getDocumentList().login(username, password);
	}
	
	/**
	 * Authenticates the client using AuthSub.
	 * 
	 * @param authSubToken the auth sub token
	 * 
	 * @throws DocumentListException the document list exception
	 * @throws AuthenticationException the authentication exception
	 */
	public void login(String authSubToken) throws AuthenticationException, DocumentListException {
		getDocumentList().loginWithAuthSubToken(authSubToken);
	}	
	
	/**
	 * Gets the root folders.
	 * 
	 * @return the root folders
	 */
	public DocumentListFeed getRootFolders() {		
		DocumentListFeed results = new DocumentListFeed();
		try {
			DocumentListFeed docs = getDocumentList().getDocsListFeed("folders");
			List<DocumentListEntry> list = new ArrayList<DocumentListEntry>();
			for (DocumentListEntry doc : docs.getEntries()) {
				if (doc.getParentLinks().isEmpty()) {
					list.add(doc);
				}			
			}
			results.setEntries(list);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	
	/**
	 * Gets the sub folders.
	 * 
	 * @param folder the folder
	 * 
	 * @return the sub folders
	 */
	public DocumentListFeed getSubFolders(DocumentListEntry folder) {
		DocumentListFeed results = null;
		if (folder == null) {
			return getRootFolders();
		}
		try {
			results = getDocumentList().getSubFolders(folder.getResourceId());
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return results;
	}
	
	/**
	 * Gets the docs.
	 * 
	 * @param folder the folder
	 * 
	 * @return the docs
	 */
	public DocumentListFeed getDocsFromFolder(DocumentListEntry folder) {
		DocumentListFeed results = null;
		if (folder == null) {
			results = new DocumentListFeed();
			DocumentListFeed docs = null;
			try {
				docs = getDocumentList().getDocsListFeed("all");
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<DocumentListEntry> list = new ArrayList<DocumentListEntry>();
			for (DocumentListEntry doc : docs.getEntries()) {
				if (doc.getParentLinks().isEmpty() && !doc.getType().equals("folder")) {
					list.add(doc);
				}			
			}
			results.setEntries(list);
		} else {
			results = new DocumentListFeed();
			DocumentListFeed docs = null;
			try {
				docs = getDocumentList().getFolderDocsListFeed(folder.getResourceId());
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<DocumentListEntry> list = new ArrayList<DocumentListEntry>();
			for (DocumentListEntry doc : docs.getEntries()) {
				if (!doc.getType().equals("folder")) {
					list.add(doc);
				}			
			}
			results.setEntries(list);					
		}
		return results;
	}
	
	/**
	 * Uploads specified folder.
	 * 
	 * @param path the path to upload
	 * @param recursive the flag for recursive upload
	 * @param replaceAll the replace all
	 * @param skipAll the skip all
	 * @param addAll the add all
	 * @param withoutFolders the without folders
	 */
	public void upload(String path, boolean recursive, boolean withoutFolders, boolean addAll, boolean skipAll, boolean replaceAll) {		
		File folder = new File(path);
		if (!folder.exists()) {
			printLine("Specified folder " + path + " doesn't exist");
			System.exit(1);			
		}
		
		printLine("\nUploading" + (recursive ? " recursively" : "") + " the folder " + path + "\n");
		
		int uploaded = uploadFolder(folder, null, recursive, withoutFolders, new Boolean(addAll), new Boolean(skipAll), new Boolean(replaceAll));
		
		printLine("\nFiles uploaded: " + uploaded);		
	}
	
	/**
	 * Internal method for uploading a folder.
	 * 
	 * @param folder the folder
	 * @param remoteFolder the remote folder
	 * @param recursive the recursive
	 * @param withoutFolders the without folders
	 * @param addAll the add all duplicates
	 * @param skipAll the skip all duplicates
	 * @param replaceAll the replace all duplicates
	 * 
	 * @return the number of uploaded documents
	 */
	protected int uploadFolder(File folder, DocumentListEntry remoteFolder, boolean recursive, boolean withoutFolders, Boolean addAll, Boolean skipAll, Boolean replaceAll) {
		folder.setReadOnly();
		DocumentListFeed remoteSubFolders = getSubFolders(remoteFolder);
		DocumentListFeed remoteDocs = getDocsFromFolder(remoteFolder);
		ArrayList<String> formats = new ArrayList<String>(Arrays.asList(SUPPORTED_FORMATS));
		int uploaded = 0;
		for (File file : folder.listFiles()) {
			if (!file.isDirectory()) {			
				printLine(file.getAbsolutePath());
				if (!formats.contains(file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase())) {
					printLine(" - Skipped: the file format is not supported");
					continue;
				}
				
				DocumentListEntry currentRemoteDoc = documentListFindByTitle(file.getName().substring(0, file.getName().lastIndexOf(".")), remoteDocs);
				boolean skip = false;
				if (currentRemoteDoc != null && !addAll) {
					boolean replace = false;
					
					if (!skipAll && !replaceAll) {
						String choice = null;
						printLine(" - A document with the same name found in Google Docs");
						
						while (true) {
							print(" - add (a) / skip (s) / replace (r) / add all (aa) / skip all (sa) / replace all (ra): ");
							Scanner scanner = new Scanner(System.in);
							choice = scanner.nextLine();
							if (choice.equals("a") || choice.equals("s") || choice.equals("r") || choice.equals("aa") || choice.equals("sa") || choice.equals("ra")) {
								break;
							}
						}
						
						if (choice.equals("s")) {
							skip = true;							
						} else if (choice.equals("r")) {
							replace = true;							
						} else if (choice.equals("aa")) {
							addAll = true;							
						} else if (choice.equals("sa")) {
							skipAll = true;							
						} else if (choice.equals("ra")) {
							replaceAll = true;							
						}
					}
					
					if (replaceAll || replace) {
						try {
							getDocumentList().trashObject(currentRemoteDoc.getResourceId(), true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}										
				}
					
				if (currentRemoteDoc == null || !skipAll && !skip) {
					for (int i = 0; i < 3; i++) {				
						try {
							if (remoteFolder == null || withoutFolders) {
								getDocumentList().uploadFile(file.getAbsolutePath(), file.getName().substring(0, file.getName().lastIndexOf(".")));
							} else {
								getDocumentList().uploadFileToFolder(file.getAbsolutePath(), file.getName().substring(0, file.getName().lastIndexOf(".")), remoteFolder.getResourceId());
							}
							uploaded++;
							break;
						} catch (InvalidEntryException e) {
							printLine(" - Skipped: " + e.getMessage());
							break;					
						} catch (Exception e) {
							printLine(" - Upload error: " + e.getMessage());
							if (i < 2) {
								printLine(" - Another try...");
							} else {
								printLine(" - Skipped");
							}
						}
					}
				} else {
					printLine(" - Skipped");
				}
			}
		}
		for (File file : folder.listFiles()) {
			if (recursive && file.isDirectory()) {
				printLine(file.getAbsolutePath());
				DocumentListEntry currentRemoteFolder = null;
				if (!withoutFolders) {
					currentRemoteFolder = documentListFindByTitle(file.getName(), remoteSubFolders);
					if (currentRemoteFolder == null) {
						try {
							if (remoteFolder == null) {
								currentRemoteFolder = getDocumentList().createNew(file.getName(), "folder");
							} else {
								currentRemoteFolder = getDocumentList().createNewSubFolder(file.getName(), remoteFolder.getResourceId());
							}						
						} catch (Exception e) {
							printLine(" - Skipped: failed to create the folder, files will be uploaded to the upper-level folder");
							e.printStackTrace();
						}			
						if (currentRemoteFolder == null) {
							printLine(" - Skipped: failed to create the folder, files will be uploaded to the upper-level folder");
						}
					}
				}
				uploaded += uploadFolder(file, currentRemoteFolder, recursive, withoutFolders, addAll, skipAll, replaceAll);
			} 
		}
		
		return uploaded;	
	}

	/**
	 * Document list find by title.
	 * 
	 * @param title the title
	 * @param documentListFeed the document list feed
	 * 
	 * @return the document list entry
	 */
	protected DocumentListEntry documentListFindByTitle(String title, DocumentListFeed documentListFeed) {
		for (DocumentListEntry doc : documentListFeed.getEntries()) {
			if (doc.getTitle().getPlainText().equals(title)) {
				return doc;
			}			
		}
		return null;
	}
	
	/**
	 * Prints out a message.
	 * 
	 * @param msg the message to be printed.
	 */
	protected static void print(String msg) {
		getOut().print(msg);
		getOut().flush();
	}
	
	/**
	 * Prints out a message.
	 * 
	 * @param msg the message to be printed.
	 */
	protected static void printLine(String msg) {
		print(msg + "\n");
	}

	/**
	 * Prints out a list of messages.
	 * 
	 * @param msg the message to be printed.
	 */
	protected static void printMessages(String[] msg) {
		for (String s : msg) {
			printLine(s);
		}
	}

	/**
	 * Gets the document list.
	 * 
	 * @return the document list
	 */
	protected DocumentList getDocumentList() {
		return documentList;
	}

	/**
	 * Sets the document list.
	 * 
	 * @param documentList the new document list
	 */
	protected void setDocumentList(DocumentList documentList) {
		this.documentList = documentList;
	}
	
	/**
	 * Gets the out.
	 * 
	 * @return the out
	 */
	protected static PrintWriter getOut() {
		if (out == null) {
			try {
				out = new PrintWriter(new OutputStreamWriter(System.out, "Cp866"), true);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return out;
	}
	
}
