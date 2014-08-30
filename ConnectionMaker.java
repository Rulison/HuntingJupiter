import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import java.awt.Graphics;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
public class ConnectionMaker extends JPanel {
	int port;
	String hostOrJoin;

	public ConnectionMaker(int port, String option)
	{
		this.port=port;
		hostOrJoin=option;
	}

}
