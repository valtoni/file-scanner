package info.boaventura.filescanner.gui;

import info.boaventura.filescanner.FindFileText;
import info.boaventura.filescanner.config.Configuration;
import info.boaventura.filescanner.lucene.IndexFiles;
import info.boaventura.filescanner.lucene.IndexFilesListener;
import info.boaventura.filescanner.lucene.LuceneSearch;
import info.boaventura.filescanner.search.GrepFinder;
import info.boaventura.filescanner.search.MatchedTextFile;
import info.boaventura.filescanner.search.MatchedTextFileSnippet;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.wb.swt.SWTResourceManager;


public class Screen {

	protected Shell shellFileScanner;
	private Text inputDirectory;
	private Text inputFilemask;
	private Text inputPattern;
	private List outputList;
	private Display display;
	private Button btnSearchImpact;
	private Button btnIndexDir;
	private Label lblFilesMatchedValue;

	private static final String SEARCH_PROGRESS = "%.0f/%.0f (%.2f%%)";
	
	private static void errorDialog(Shell shell, String message) {
		new MessageDialog(shell, "ERRO", null, message, MessageDialog.ERROR, new String[] { "OK" }, 0).open();
	}
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Screen window = new Screen();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shellFileScanner.open();
		shellFileScanner.layout();
		Icons.load(shellFileScanner);
		while (!shellFileScanner.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private Logger log = Logger.getLogger(getClass());
	
	protected String loadProperty(String key) {
		Properties properties = new Properties();
		try {
	    properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
    } catch (Exception e) {
    	log.error("Cannot load " + key + ": " + e.getMessage());
	    return "--cannnot load " + key + "--";
    }
		return properties.getProperty(key);
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shellFileScanner = new Shell();
		shellFileScanner.setSize(751, 530);
		shellFileScanner.setText("File Scanner (http://boaventura.info) v" + loadProperty("version"));
		shellFileScanner.setLayout(new FormLayout());
		
		inputDirectory = new Text(shellFileScanner, SWT.BORDER);
		inputDirectory.setText("");
		FormData fd_inputDirectory = new FormData();
		fd_inputDirectory.left = new FormAttachment(0, 10);
		inputDirectory.setLayoutData(fd_inputDirectory);
		
		Label lblNewLabel = new Label(shellFileScanner, SWT.NONE);
		fd_inputDirectory.top = new FormAttachment(lblNewLabel, 6);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.bottom = new FormAttachment(0, 25);
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Directory");
		
		Label lblFileMask = new Label(shellFileScanner, SWT.NONE);
		fd_inputDirectory.bottom = new FormAttachment(lblFileMask, -6);
		lblFileMask.setText("File Mask");
		FormData fd_lblFileMask = new FormData();
		fd_lblFileMask.left = new FormAttachment(0, 10);
		fd_lblFileMask.top = new FormAttachment(0, 58);
		lblFileMask.setLayoutData(fd_lblFileMask);
		
		inputFilemask = new Text(shellFileScanner, SWT.BORDER);
		fd_inputDirectory.right = new FormAttachment(inputFilemask, 0, SWT.RIGHT);
		inputFilemask.setText("**/*.{gsp,groovy,css,xhtml,html,htm,tld,nsi,bat,sh,py,java,xml,properties}");
		FormData fd_inputFilemask = new FormData();
		fd_inputFilemask.right = new FormAttachment(0, 232);
		fd_inputFilemask.top = new FormAttachment(lblFileMask, 6);
		fd_inputFilemask.left = new FormAttachment(0, 10);
		inputFilemask.setLayoutData(fd_inputFilemask);
		
		Label lblPattern = new Label(shellFileScanner, SWT.NONE);
		lblPattern.setText("Pattern (Regular Expression)");
		FormData fd_lblPattern = new FormData();
		fd_lblPattern.top = new FormAttachment(0, 58);
		fd_lblPattern.left = new FormAttachment(lblFileMask, 179);
		lblPattern.setLayoutData(fd_lblPattern);
		
		inputPattern = new Text(shellFileScanner, SWT.BORDER);
		FormData fd_inputPattern = new FormData();
		fd_inputPattern.top = new FormAttachment(lblFileMask, 6);
		fd_inputPattern.left = new FormAttachment(lblFileMask, 179);
		fd_inputPattern.right = new FormAttachment(100, -10);
		inputPattern.setLayoutData(fd_inputPattern);

		btnSearchImpact = new Button(shellFileScanner, SWT.NONE);
		btnSearchImpact.addMouseListener(runSearch());
		FormData fd_btnSearchImpact = new FormData();
		fd_btnSearchImpact.top = new FormAttachment(0, 106);
		fd_btnSearchImpact.right = new FormAttachment(100, -10);
		btnSearchImpact.setLayoutData(fd_btnSearchImpact);
		btnSearchImpact.setText("Search Impact");
		btnSearchImpact.pack();

		btnIndexDir = new Button(shellFileScanner, SWT.NONE);
		btnIndexDir.addMouseListener(runCreateIndex());
		FormData fd_btnIndexDir = new FormData();
		fd_btnIndexDir.bottom = new FormAttachment(btnSearchImpact, 0, SWT.BOTTOM);
		fd_btnIndexDir.left = new FormAttachment(lblPattern, 0, SWT.LEFT);
		btnIndexDir.setLayoutData(fd_btnIndexDir);
		btnIndexDir.setText("Index Dir");
		
		Label lblFilesMatched = new Label(shellFileScanner, SWT.NONE);
		FormData fd_lblFilesMatched = new FormData();
		fd_lblFilesMatched.top = new FormAttachment(btnSearchImpact, 5, SWT.TOP);
		fd_lblFilesMatched.left = new FormAttachment(0, 98);
		lblFilesMatched.setLayoutData(fd_lblFilesMatched);
		lblFilesMatched.setText("Files Matched:");
		
		lblFilesMatchedValue = new Label(shellFileScanner, SWT.NONE);
		lblFilesMatchedValue.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		FormData fd_lblFilesMatchedValue = new FormData();
		fd_lblFilesMatchedValue.right = new FormAttachment(inputFilemask, 0, SWT.RIGHT);
		fd_lblFilesMatchedValue.top = new FormAttachment(btnSearchImpact, 5, SWT.TOP);
		fd_lblFilesMatchedValue.left = new FormAttachment(lblFilesMatched, 6);
		lblFilesMatchedValue.setLayoutData(fd_lblFilesMatchedValue);
		lblFilesMatchedValue.setText("0");
		
		TabFolder tabFolder = new TabFolder(shellFileScanner, SWT.NONE);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.left = new FormAttachment(0, 10);
		fd_tabFolder.right = new FormAttachment(100, -10);
		fd_tabFolder.top = new FormAttachment(btnSearchImpact, 6);
		fd_tabFolder.bottom = new FormAttachment(100, -33);
		tabFolder.setLayoutData(fd_tabFolder);
		
		TabItem tbtmOutput = new TabItem(tabFolder, SWT.NONE);
		tbtmOutput.setText("Output");
		
		outputList = new List(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tbtmOutput.setControl(outputList);
		
		tbtmSource = new TabItem(tabFolder, SWT.NONE);
		tbtmSource.setText("Source");
		
		outputImpact = new List(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		outputImpact.addMouseListener(onDoubleClickOpenFile());
		tbtmSource.setControl(outputImpact);
		
		TabItem tbtmImpact = new TabItem(tabFolder, SWT.NONE);
		tbtmImpact.setText("Impact");
		
		treeImpact = new Tree(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tbtmImpact.setControl(treeImpact);
		
		// Setups footer
		Composite footer = new Composite(shellFileScanner, SWT.NONE);
		FormData fd_footer = new FormData();
		fd_footer.left = new FormAttachment(0);
		fd_footer.right = new FormAttachment(100);
		fd_footer.top = new FormAttachment(tabFolder, 6);
		fd_footer.bottom = new FormAttachment(100);
		footer.setLayoutData(fd_footer);
		
		// Setups progressbar
		progressBar = new ProgressBar(footer, SWT.NONE);
		progressBar.setBounds(173, 5, 552, 17);

		// Setups label of progress
		labelProgress = new Label(footer, SWT.NONE);
		labelProgress.setBounds(10, 7, 157, 15);
		labelProgress.setText("0/0 (0%)");
		
		inputContainerIndexDir = new Text(shellFileScanner, SWT.BORDER);
		inputContainerIndexDir.setText(Configuration.APP_HOME.toString());
		FormData fd_inputContainerIndexDir = new FormData();
		fd_inputContainerIndexDir.right = new FormAttachment(inputPattern, 0, SWT.RIGHT);
		fd_inputContainerIndexDir.bottom = new FormAttachment(lblPattern, -6);
		fd_inputContainerIndexDir.left = new FormAttachment(inputDirectory, 6);
		inputContainerIndexDir.setLayoutData(fd_inputContainerIndexDir);
		
		Label lblIndexDirectory = new Label(shellFileScanner, SWT.NONE);
		FormData fd_lblIndexDirectory = new FormData();
		fd_lblIndexDirectory.bottom = new FormAttachment(lblNewLabel, 0, SWT.BOTTOM);
		fd_lblIndexDirectory.left = new FormAttachment(lblPattern, 0, SWT.LEFT);
		lblIndexDirectory.setLayoutData(fd_lblIndexDirectory);
		lblIndexDirectory.setText("Container Index Directory");
		
		lblglobPattern = new Label(shellFileScanner, SWT.NONE);
		FormData fd_lblglobPattern = new FormData();
		fd_lblglobPattern.top = new FormAttachment(inputDirectory, 6);
		fd_lblglobPattern.left = new FormAttachment(lblFileMask, 6);
		lblglobPattern.setLayoutData(fd_lblglobPattern);
		lblglobPattern.setText("(Glob pattern)");
		
    // Setups findFile
    findFileText = new FindFileText();
		findFileText.addObserver(observerSearchEvents);
		
	}

	FindFileText findFileText;
	IndexFiles indexFiles;
	
	private MouseAdapter onDoubleClickOpenFile() {
	  return new MouseAdapter() {
	    @Override
	    public void mouseDoubleClick(MouseEvent e) {
	      super.mouseDown(e);
	      String[] selection = outputImpact.getSelection();
	      try {
  	      if (selection.length > 0) {
  	        String absolute = selection[0];
  	        if (absolute.matches("^\\s+")) {
  	          throw new RuntimeException("Please click in file");
  	        }
  	        // JDIC integration
  	        Desktop desktop = Desktop.getDesktop();
  	        if (!desktop.isSupported(Action.EDIT)) {
  	          throw new RuntimeException("This OS doesn't suport edit action");
  	        }
  	        desktop.open(new File(inputDirectory.getText() + File.separator + absolute));
  	      }
	      } catch (Exception ex) {
	        errorDialog(shellFileScanner, ex.getMessage());
	      }
	    }
    };
	}
	
	private MouseAdapter runCreateIndex() {
		return new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					outputList.removeAll();
					outputImpact.removeAll();
					treeImpact.removeAll();
					
					indexFiles = new IndexFiles(inputDirectory.getText(), inputContainerIndexDir.getText(), inputFilemask.getText(), false);
					indexFiles.setListener(indexFilesListener);
					
					Thread thread = new Thread(indexFiles);
					lblFilesMatchedValue.setText("0");
					resetProgress();
					changeEntry();
					thread.start();
					
				} catch (Exception e1) {
					errorDialog(shellFileScanner, "Erro: " + e1.getMessage());
				}
			}
		};

  }
	
	private MouseAdapter runSearch() {
		return new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					outputList.removeAll();
					outputImpact.removeAll();
					treeImpact.removeAll();
					
					findFileText.setup(inputDirectory.getText(), inputFilemask.getText(), inputPattern.getText());
					
					java.util.List<String> results;
					try (LuceneSearch search = new LuceneSearch(inputContainerIndexDir.getText())) {
						results = search.searchStr(
								new String[] { info.boaventura.filescanner.lucene.File.FIELD_PATH,  info.boaventura.filescanner.lucene.File.FIELD_CONTENTS }, 
								inputPattern.getText());
						addOutputList(results);
					}
					
					GrepFinder grep = new GrepFinder(results);
					grep.setPattern(inputPattern.getText());
					grep.find();
							
					for (MatchedTextFile matchedTextFile: grep.getMatchedTexts()) {
						String file = matchedTextFile.getFile().getAbsolutePath().replace(inputDirectory.getText() + File.separator, "");
						outputImpact.add(file);
						for (MatchedTextFileSnippet snippet: matchedTextFile.getMatches()) {
							outputImpact.add(String.format("  %d: %s", snippet.getLine(), snippet.getTextLine()));
						}
					}
					
					/*Thread thread = new Thread(findFileText);
					lblFilesMatchedValue.setText("0");
					resetProgress();
					changeEntry();
					thread.start();*/
					
				} catch (Exception e1) {
					errorDialog(shellFileScanner, "Erro: " + e1.getMessage());
				}
			}
		};
	}
	
	private void changeEntry() {
		display.syncExec(
				new Runnable() {
					public void run() {
						boolean flag = !inputDirectory.getEnabled();
						inputDirectory.setEnabled(flag);
						inputFilemask.setEnabled(flag);
						inputPattern.setEnabled(flag);
						btnSearchImpact.setEnabled(flag);
						btnIndexDir.setEnabled(flag);
					}
				}
			);
	}
	
	private void changeAffectedFiles() {
		display.syncExec(
				new Runnable() {
					public void run() {
						lblFilesMatchedValue.setText("" + affectedFiles);
						progressBar.setMaximum(affectedFiles);
					}
				}
			);
	}
	
	public static double truncate(double value, int places) {
    if (places < 0) {
        throw new IllegalArgumentException();
    }
    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = (long) value;
    return (double) tmp / factor;
	}
	
	String messageStatus(double step, double max) {
	  if (max > 0) {
	    return String.format(SEARCH_PROGRESS, step, max, (step / max) * 100);
	  }
	  return String.format(SEARCH_PROGRESS, 0d, 0d, 0.0);
	}
	
	private void resetProgress() {
		display.syncExec(
				new Runnable() {
					public void run() {
						progressBar.setSelection(0);
						labelProgress.setText(messageStatus(0, 0));
					}
				}
			);
	}
	
	private void increaseProgressBar() {
		display.syncExec(
				new Runnable() {
					public void run() {
						progressBar.setSelection(progressBar.getSelection() + 1);
						labelProgress.setText(messageStatus(progressBar.getSelection(), progressBar.getMaximum()));
					}
				}
			);
	}
	
	private int affectedFiles;
	
	private IndexFilesListener indexFilesListener = new IndexFilesListener() {
		
		int countDocs = 0;
		
		int countPaths = 0;
		
		@Override
		public void onStartDoIndex() {
			countDocs = 0;
			countPaths = 0;
			addOutputList("Indexação iniciada");
		}
		
		@Override
		public void onError(Throwable error) {
			addOutputList("Erro ao efetuar indexação: " + error.getMessage());
		}
		
		@Override
		public void onEndDoIndex() {
			addOutputList("Indexação terminada: " + countDocs + " documentos indexados");
			changeEntry();
		}
		
		@Override
		public void beforeIndexPath(Path path) {
			display.syncExec(
					new Runnable() {
						public void run() {
							progressBar.setMaximum(++countPaths);
							lblFilesMatchedValue.setText("" + countPaths);
						}
					}
				);
		}
		
		@Override
		public void afterIndexPath(Path path) {
			addOutputList("Path encontrado: " + path.toString());
			increaseProgressBar();
		}
		
		@Override
		public void afterAddDocument(Document document) {
			countDocs++;
		}
		
	};
	
	
	private Observer observerSearchEvents = new Observer() {
		
		@Override
		@SuppressWarnings("unchecked")
		public void update(Observable paramObservable, Object paramObject) {
			FindFileText.Event event = (FindFileText.Event)paramObject;
			if (event.getType() == FindFileText.EventType.COUNT_ENTRIES) {
				affectedFiles = ((java.util.List<File>)event.getWrapper()).size();
				changeAffectedFiles();
			}
			else if (event.getType() == FindFileText.EventType.COUNT_MATCHED_TEXT) {
				addOutputList(convertListMatch((java.util.List<MatchedTextFile>)event.getWrapper()));
			}
			else if (event.getType() == FindFileText.EventType.COUNT_FILE_SEARCH) {
				increaseProgressBar();
			}
			else if (event.getType() == FindFileText.EventType.TERMINATED) {
				addResults(((java.util.List<MatchedTextFile>)event.getWrapper()));
				// Habilita controles
				changeEntry();
			}
		}

		private void addResults(final java.util.List<MatchedTextFile> list) {
			display.syncExec(
					new Runnable() {
						
						public void run() {
							try {
								String file;

								java.util.List<String> fileListed = new ArrayList<String>(); 
								for (MatchedTextFile matchedTextFile: list) {
									file = matchedTextFile.getFile().getAbsolutePath().replace(inputDirectory.getText() + File.separator, "");
									outputImpact.add(file);
									fileListed.add(file);
									for (MatchedTextFileSnippet snippet: matchedTextFile.getMatches()) {
										outputImpact.add(String.format("  %d: %s", snippet.getLine(), snippet.getTextLine()));
									}
								}
								
							} catch (Exception e) {
								errorDialog(shellFileScanner, e.getMessage());
							}
						}
					}
				);
		}

	};
	private TabItem tbtmSource;
	private List outputImpact;
	private Tree treeImpact;
	private ProgressBar progressBar;
	private Label labelProgress;
	private Text inputContainerIndexDir;
	private Label lblglobPattern;
	
	private void addOutputList(final java.util.List<String> list) {
		display.syncExec(
				new Runnable() {
					public void run() {
						for (String entry: list) {
							addOutputList(entry);
						}
					}
				}
		);
	}
	
	private void addOutputList(final String entry) {
		display.syncExec(
				new Runnable() {
					public void run() {
						outputList.add(entry);
						// Roll to end
						outputList.setTopIndex(outputList.getItemCount() - 1);
					}
				}
		);
	}
	
	private java.util.List<String> convertListMatch(final java.util.List<MatchedTextFile> matchedTextFiles) {
		java.util.List<String> list = new ArrayList<String>();
		display.syncExec(
				new Runnable() {
					public void run() {
				
						for (MatchedTextFile matchedTextFile: matchedTextFiles) {
							outputList.add(matchedTextFile.getFile().getAbsolutePath() + " (" + matchedTextFile.getMatches().size() + ")");
				/*			for (MatchedTextFileSnippet matchedTextFileSnippet: matchedTextFile.getMatches()) {
				
							}*/
						}
					}
				}
		);
						
		return list;
	}
	

}
