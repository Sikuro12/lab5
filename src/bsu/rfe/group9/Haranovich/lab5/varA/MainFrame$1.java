package bsu.rfe.group9.Haranovich.lab5.varA;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class MainFrame$1 extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (MainFrame.access$0(MainFrame.this) == null) {
            MainFrame.access$1(MainFrame.this, new JFileChooser());
            MainFrame.access$0(MainFrame.this).setCurrentDirectory(new File("."));
        }
        MainFrame.access$0(MainFrame.this).showOpenDialog(MainFrame.this);
        MainFrame.this.openGraphics(MainFrame.access$0(MainFrame.this).getSelectedFile());
    }
}