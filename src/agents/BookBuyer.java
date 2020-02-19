package agents;

import java.awt.TextArea;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class BookBuyer extends Agent{

	
	//filled from the service
	ArrayList<AID> sellersAID= new ArrayList<AID>();
	
	//names of the books
	ArrayList<String> booksName=new ArrayList<String>();
	
	//acquire book price from service
	HashMap<Double,String> bookPrices=new HashMap<Double,String>();
	
	
	private int SELLERS_NUMBER=0;
	private int SECONDS_NUMBER=5000;
	private AID chosenSeller;
	private String bookName;
	
	int i =0;
	@Override
	protected void setup() {
		
		
		
		initBooks();
		
		//get the text area to show the messages
		TextArea textPane=(TextArea)getArguments()[0];
		
		//get the number of sellers from the arguments
		SELLERS_NUMBER=(int)getArguments()[1];
		SECONDS_NUMBER=(int)getArguments()[2];
		bookName=(String)getArguments()[3];
		
		addBehaviour(new OneShotBehaviour() {
			
			@Override
			public void action() {
			DFAgentDescription template= new DFAgentDescription();
			ServiceDescription sd=new ServiceDescription();
			sd.setType("Book-Selling");
			try {
				
				DFAgentDescription[] results = DFService.search(myAgent, template);
				for(DFAgentDescription df :results) {
					sellersAID.add(df.getName());
				}
				
			} catch (FIPAException e) {
				e.printStackTrace();
				}
			}
		});
		
		addBehaviour(new TickerBehaviour(this,SECONDS_NUMBER * 1000) {
			
			@Override
			protected void onTick() {
				
				
				int min=0;
				int max =booksName.size()-1;
				int range=max - min + 1;
				
				int index= (int)(Math.random()* range) + min;
				
					String string="";
					
				if(!bookName.equalsIgnoreCase("random")) 
					 string ="Buyer asking for "+bookName;	
				else 
					 string="Buyer asking for "+booksName.get(index);
				
				if(textPane != null) {
					textPane.append(string+"\n");							
				}
				for (int i = 0; i < sellersAID.size(); i++) 
				{
					
					ACLMessage request = new ACLMessage(ACLMessage.CFP);
					request.addReceiver(sellersAID.get(i));
					
					if(!bookName.equalsIgnoreCase("random")) {
						request.setContent(bookName);
					}else
						request.setContent(booksName.get(index));	
						
					
					send(request);
				}
				//empty the prices for the next comparison
				bookPrices.clear();
			}
		});
		
		addBehaviour(new CyclicBehaviour() {
			
			@Override
			public void action() {
				ACLMessage response=receive();
				
				
				 if (response!=null && response.getConversationId() != null
						 &&response.getConversationId().equalsIgnoreCase("negotiate")) {
				
						ACLMessage accept_proposal= new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						double price = Double.parseDouble(response.getContent());
						accept_proposal.addReceiver(chosenSeller);
						accept_proposal.setContent("i accept book for "+ price+"DZD");
						send(accept_proposal);
						block();
						
				}else if (response!=null && response.getPerformative() == ACLMessage.PROPOSE) {
						double price=Double.parseDouble(response.getContent());
						System.out.println(getLocalName()+": received price is : "+price);
						bookPrices.put(price,response.getSender().getLocalName()+"");
			
						if(!bookPrices.isEmpty() && bookPrices.size() == SELLERS_NUMBER) {
							ArrayList<Double> prices= new ArrayList<Double>(bookPrices.keySet());
							double min =0;
							for(int i = 0 ; i<prices.size();i++) {
								if(min > prices.get(i) || min == 0) {
									min=prices.get(i);
								}
							}		
							chosenSeller=new AID(bookPrices.get(min),AID.ISLOCALNAME);
							ACLMessage negotiate=new ACLMessage(ACLMessage.PROPOSE);
							negotiate.addReceiver(chosenSeller);
							negotiate.setContent(min+"");
							textPane.append(myAgent.getLocalName()+" asks the "+chosenSeller.getLocalName()+" for reduction of price\n");
							negotiate.setConversationId("negotiate");
							send(negotiate);
							block();
							
						}
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
		}
}
