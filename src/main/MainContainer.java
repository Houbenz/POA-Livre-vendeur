package main;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import jade.core.Profile; 
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JComboBox;

public class MainContainer{
	private JFrame frame;
	private  JButton startMainBtn;
	private  JButton startBuyersBtn;
	private  JButton startSellersBtn;
	
	static TextArea textPane;
	AgentContainer mainContainer;
	AgentContainer buyersContainer;
	AgentContainer sellersContainer;
	
	private static JSpinner spinner_sellers;
	private JLabel lblNewLabel;
	private JSpinner spinner_seconds;
	private JLabel lblNumberOfSeconds;
	private  int sellers_number=5;
	private  int seconds_number=5;
	private JComboBox comboBox;
	private JLabel lblBookToLook;
	private ArrayList<String> booksName=new ArrayList<String>();
	private String bookName;

	
	public MainContainer() {
		initialize();
	}
	
	
	public static void main(String[] args) {	
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainContainer window = new MainContainer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1080, 720);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[][][grow]", "[][][][][][][grow]"));
		
		 startMainBtn = new JButton("Start main container");
		startMainBtn.setFont(new Font("Dialog", Font.BOLD, 10));
		panel.add(startMainBtn, "cell 0 1,growx");
		
		startSellersBtn = new JButton("Start Sellers container");
		startSellersBtn.setFont(new Font("Dialog", Font.BOLD, 10));
		panel.add(startSellersBtn, "cell 0 2,growx");
		
		lblNewLabel = new JLabel("Number of sellers");
		panel.add(lblNewLabel, "cell 1 2");
		
		spinner_sellers = new JSpinner();
		panel.add(spinner_sellers, "cell 2 2,alignx left");
		
		spinner_sellers.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent event) {
				sellers_number=(int)spinner_sellers.getValue();
				updateUIWhenSpinnersOrComboBox();
			}
		});
		
		startBuyersBtn = new JButton("Start Buyers container");
		startBuyersBtn.setFont(new Font("Dialog", Font.BOLD, 10));
		panel.add(startBuyersBtn, "cell 0 3,growx");
		
		
		lblNumberOfSeconds = new JLabel("Number of seconds");
		panel.add(lblNumberOfSeconds, "cell 1 3");
		
		spinner_seconds = new JSpinner();
		panel.add(spinner_seconds, "cell 2 3,alignx left");
		
		spinner_seconds.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
					seconds_number=(int)spinner_seconds.getValue();
					updateUIWhenSpinnersOrComboBox();
			}
		});
		
		lblBookToLook = new JLabel("Book to look for");
		panel.add(lblBookToLook, "cell 0 4,alignx center");
		
		initBooks();
		comboBox = new JComboBox(booksName.toArray());
		panel.add(comboBox, "cell 0 5,growx");
		bookName=(String)comboBox.getItemAt(0);
		comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					JComboBox cb=(JComboBox)e.getSource();
					bookName=(String)cb.getSelectedItem();
					updateUIWhenSpinnersOrComboBox();
					
			}
		});
		
		
		textPane = new TextArea();
		panel.add(textPane, "cell 1 6 2 1,grow");	
		textPane.setText("Action appears here : \n");
		Font font = textPane.getFont();
		float size = font.getSize()+5.0f;
		textPane.setFont(font.deriveFont(size));
		
		
		addListenerToMainContainer();
		addListenerToSellersContainer();
		addListenerToBuyersContainer();
		

	}
	
	private void updateUIWhenSpinnersOrComboBox() {
		try {
				if(sellersContainer!=null && sellersContainer.isJoined() == true)
					sellersContainer.kill();
				if(buyersContainer!=null && buyersContainer.isJoined() == true)
					buyersContainer.kill();
				
				startSellersBtn.setText("Start Sellers container");
				startBuyersBtn.setText("Start Buyers container");
				//textPane.setText("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addListenerToMainContainer() {
		startMainBtn.addActionListener(ActionEvent ->{
			if(startMainBtn.getText().equalsIgnoreCase("Start Main container")) {
				startMainBtn.setText("Stop Main container");

			try {
			Runtime runtime=Runtime.instance();
			Properties properties=new ExtendedProperties();
			properties.setProperty(Profile.GUI,"true");
			ProfileImpl profileImpl=new ProfileImpl(properties);
			mainContainer=runtime.createMainContainer(profileImpl);
			mainContainer.start();
			} catch (Exception e) {e.printStackTrace();}
			
			}else {
				startMainBtn.setText("Start Main container");

				try {
					mainContainer.kill();
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void addListenerToSellersContainer() {
		

		startSellersBtn.addActionListener(actionEvent ->{
			if(startSellersBtn.getText().equalsIgnoreCase("Start Sellers container")) {
				
			try {
				startSellersBtn.setText("Stop Sellers container");
				Runtime runtime2=Runtime.instance();
				ProfileImpl profileImpl2=new ProfileImpl(false);
				profileImpl2.setParameter(ProfileImpl.MAIN_HOST,"localhost");
				sellersContainer=runtime2.createAgentContainer(profileImpl2);
		
				
				for (int i = 1; i < sellers_number + 1 ; i++) {
					AgentController seller1=sellersContainer.
							createNewAgent("seller"+i,"agents.BookSeller", 
									new Object[] {textPane,sellers_number,seconds_number});
					seller1.start();
				}
				sellersContainer.start();
			
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			}else {

				startSellersBtn.setText("Start Sellers container");
				try {
					sellersContainer.kill();
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}
			}
		});	
	}
	private void addListenerToBuyersContainer() {
		startBuyersBtn.addActionListener(actionEvent -> {
			if(startBuyersBtn.getText().equalsIgnoreCase("Start Buyers container")) {
				
				
			try {
				startBuyersBtn.setText("Stop Buyers container");
			Runtime runtime1=Runtime.instance();
			ProfileImpl profileImpl1=new ProfileImpl(false);
			profileImpl1.setParameter(ProfileImpl.MAIN_HOST,"localhost");
			buyersContainer=runtime1.createAgentContainer(profileImpl1);
			
			AgentController buyer1=buyersContainer.createNewAgent("buyer1", "agents.BookBuyer",
					new Object[] {textPane,sellers_number,seconds_number,bookName});
			
			buyer1.start();
			buyersContainer.start();
		} catch (Exception e) {e.printStackTrace();}
			
			}else {

				startBuyersBtn.setText("Start Buyers container");
				try {
					buyersContainer.kill();
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void initBooks() {
		booksName.add("XML");
		booksName.add("JAVA");
		booksName.add("UBUNTU");
		booksName.add("WINDOWS");
		booksName.add("LARAVEL");
		booksName.add("RANDOM");
		}
}
