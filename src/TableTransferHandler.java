import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

public class TableTransferHandler extends TransferHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int getSourceActions(JComponent c) {
		return DnDConstants.ACTION_COPY_OR_MOVE;
	}

	public Transferable createTransferable(JComponent comp) {
		JTable table = (JTable) comp;
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();

		String value = (String) table.getModel().getValueAt(row, col);
		StringSelection transferable = new StringSelection(value);
		table.getModel().setValueAt(null, row, col);
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

		int row = dl.getRow();
		int col = dl.getColumn();

		String data;
		try {
			data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		tableModel.setValueAt(data, row, col);

		return true;
	}
}
