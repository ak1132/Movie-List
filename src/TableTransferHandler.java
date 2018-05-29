import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

public class TableTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;

	private int sourceRow = 0;

	public int getSourceActions(JComponent c) {
		return DnDConstants.ACTION_COPY_OR_MOVE;
	}

	public Transferable createTransferable(JComponent comp) {

		List<String> selectedRowAttr = new ArrayList<String>();
		JTable table = (JTable) comp;
		sourceRow = table.getSelectedRow();

		for (int i = 0; i < table.getColumnCount(); i++) {
			String value = (String) table.getModel().getValueAt(sourceRow, i);
			selectedRowAttr.add(value);
		}

		StringSelection transferable = new StringSelection(Arrays.toString(selectedRowAttr.toArray()));
		return transferable;
	}

	public boolean canImport(TransferHandler.TransferSupport info) {
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}

		return true;
	}

	public boolean importData(TransferSupport support) {

		if (!support.isDrop()) {
			return false;
		}

		if (!canImport(support)) {
			return false;
		}

		JTable table = (JTable) support.getComponent();
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

		JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();

		int afterrow = dl.getRow();
		int column = dl.getColumn();

		tableModel.getValueAt(afterrow, column);

		String data;
		String microData[] = null;
		try {
			data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
			if (data != null || data != "") {
				data = data.substring(1, data.length() - 2);
				microData = data.split(",");
			}
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		for (int i = 0; i < microData.length; i++) {
			String val = (String) tableModel.getValueAt(afterrow, i);
			tableModel.setValueAt(val, sourceRow, i);
			tableModel.setValueAt(microData[i], afterrow, i);
		}

		return true;
	}
}
