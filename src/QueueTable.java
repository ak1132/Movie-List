import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class QueueTable extends JPanel {

	DefaultTableModel tableModel;
	JTable table;
	static JFrame frame;

	public QueueTable() throws IOException, SAXException, ParserConfigurationException {
		super(new GridBagLayout());

		File f = new File("movie_data.csv");

		refreshComponents(false, f);

		addTableComponents();

	}

	public void addTableComponents() {

		JScrollPane scrollPane = new JScrollPane(table);

		add(scrollPane);

		JButton submitButton = new JButton();
		submitButton.setText("Save as XML");
		add(submitButton);

		submitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					QueueTable.saveToXMlDom(tableModel);
				} catch (TransformerException e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton up = new JButton();
		up.setText("Up");

		up.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow - 1 >= 0) {
					tableModel.moveRow(selectedRow, selectedRow, selectedRow - 1);
					table.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
				}
				table.setSelectionBackground(Color.LIGHT_GRAY);
			}
		});

		JButton down = new JButton();
		down.setText("Down");
		down.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = table.getSelectedRow();
				int rowCount = table.getRowCount();
				if (selectedRow + 1 < rowCount) {
					tableModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
					table.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
				}
				table.setSelectionBackground(Color.LIGHT_GRAY);
			}
		});

		JButton importB = new JButton();
		importB.setText("Import");
		importB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File file = selectFile(frame, new File(""), QueueTable.this, "Import file");
				try {
					if (file != null) {
						refreshComponents(true, file);
					} else {
						JOptionPane.showMessageDialog(null, "Please select a valid file");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (SAXException e1) {
					e1.printStackTrace();
				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
				}
			}
		});

		JButton addB = new JButton();
		addB.setText("Add");
		addB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.addRow(new String[4]);
				table.requestFocus();
				table.changeSelection(tableModel.getRowCount() - 1, 0, false, false);
				table.editCellAt(tableModel.getRowCount() - 1, 0);
				tableModel.fireTableDataChanged();
			}

		});

		JButton delete = new JButton();
		delete.setText("Delete");
		delete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if (row > 0)
					tableModel.removeRow(row);
				tableModel.fireTableDataChanged();
			}

		});

		add(up);
		add(down);
		add(importB);
		add(addB);
		add(delete);
	}

	public String[] refreshComponents(boolean exists, File file)
			throws IOException, SAXException, ParserConfigurationException {

		String[] columnNames = null;

		if (!exists) {

			BufferedReader sc = new BufferedReader(new FileReader(file));
			columnNames = sc.readLine().split(",");

			Object[] tableLines = sc.lines().toArray();
			tableModel = new DefaultTableModel();
			tableModel.setColumnIdentifiers(columnNames);

			table = new JTable(tableModel) {
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			table.setDragEnabled(true);
			table.setDropMode(DropMode.USE_SELECTION);

			table.setTransferHandler(new TableTransferHandler());

			table.setPreferredScrollableViewportSize(new Dimension(500, 500));
			table.setFillsViewportHeight(true);
			tableModel.setColumnIdentifiers(columnNames);
			for (int i = 0; i < tableLines.length; i++) {
				tableModel.addRow(tableLines[i].toString().split(","));

			}

			sc.close();

		} else {
			tableModel = (DefaultTableModel) table.getModel();
			tableModel.getDataVector().clear();
			tableModel.fireTableDataChanged();

			String[][] tableLines = parseXMLDom(file);

			for (int i = 0; i < tableLines.length; i++) {
				String[] obj = tableLines[i];
				tableModel.addRow(obj);
			}
			tableModel.fireTableDataChanged();
			table.repaint();

		}

		return columnNames;
	}

	public static void createUI() throws IOException, SAXException, ParserConfigurationException {
		frame = new JFrame("Netflix Queue");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		QueueTable table = new QueueTable();
		table.setOpaque(true);
		frame.setContentPane(table);

		frame.pack();
		frame.setVisible(true);
	}

	public static File selectFile(JFrame frame, File file, JComponent component, String title) {

		File selectedFile = null;

		if ("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
				.equalsIgnoreCase(UIManager.getSystemLookAndFeelClassName())) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			String absPath = file.getAbsolutePath();
			fc.setSelectedFile(new File(absPath));

			int returnVal = fc.showOpenDialog(component);
			fc.addChoosableFileFilter(new FileFilter() {

				@Override
				public boolean accept(File file) {
					String filename = file.getName();
					return filename.endsWith(".xml");
				}

				@Override
				public String getDescription() {
					return "*.xml";
				}
			});

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				selectedFile = fc.getSelectedFile();
			}
		} else {
			System.setProperty("apple.awt.fileDialogForDirectories", "false");

			FileDialog fd = new FileDialog(frame, title, FileDialog.LOAD);
			String absPath = file.getAbsolutePath();
			fd.setDirectory(absPath);
			fd.setFile(absPath);

			fd.setFilenameFilter(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml");
				}
			});

			fd.setVisible(true);

			if (fd.getFile() != null) {
				String selectedFileName = fd.getDirectory() + File.separator + fd.getFile();
				selectedFile = new File(selectedFileName);
			}
		}

		return selectedFile;
	}

	public static String[][] parseXMLDom(File f) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(f);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("movie");
		String[][] data = new String[nList.getLength()][4];

		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			String arr[] = new String[4];

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) node;

				arr[0] = element.getAttribute("name");
				arr[1] = element.getAttribute("budget");
				arr[2] = element.getAttribute("imdbScores");
				arr[3] = element.getAttribute("gross");
			}
			data[i] = arr;

		}
		return data;
	}

	public static void saveToXMlDom(DefaultTableModel model) throws TransformerException {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			int row = model.getRowCount();

			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("movieInfo");
			doc.appendChild(rootElement);

			for (int i = 0; i < row; i++) {

				Element movie = doc.createElement("movie");
				rootElement.appendChild(movie);

				movie.setAttribute("id", i + 1 + "");
				movie.setAttribute("name", model.getValueAt(i, 0).toString());
				movie.setAttribute("budget", model.getValueAt(i, 1).toString());
				movie.setAttribute("imdbScores", model.getValueAt(i, 2).toString());
				movie.setAttribute("gross", model.getValueAt(i, 3).toString());

			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("//Users//amoghkulkarni//eclipse-workspace//movie.xml"));

			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

}
