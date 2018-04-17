package ru.msakhterov.rs_client.view;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.List;

public class DragAndDropListener implements DropTargetListener {

    private ClientGUI client;

    DragAndDropListener(ClientGUI client) {
        this.client = client;
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        event.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable transferable = event.getTransferable();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (DataFlavor flavor : flavors) {
            try {
                if (flavor.isFlavorJavaFileListType()) {
                    List files = (List) transferable.getTransferData(flavor);
                    for (Object file : files) {
                        client.uploadDraggedFile(file.toString());
                        JOptionPane.showMessageDialog(null, file);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        event.dropComplete(true);
    }

    @Override
    public void dragEnter(DropTargetDragEvent event) {
    }

    @Override
    public void dragExit(DropTargetEvent event) {
    }

    @Override
    public void dragOver(DropTargetDragEvent event) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent event) {
    }
}

