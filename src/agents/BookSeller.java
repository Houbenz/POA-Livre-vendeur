package agents;

import java.awt.TextArea;
import java.util.HashMap;


import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class BookSeller extends Agent{
	
	
	HashMap<String, Integer> books=new HashMap<String,Integer>();
	private double price;
	
	@Override
	protected void setup() {
				
		
		TextArea textPane =(TextArea) getArguments()[0];
		
		initBooks();

		DFAgentDescription dfd =new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd= new ServiceDescription();
		sd.setType("Book-Selling");
		sd.setName("seller-book-trading");
		dfd.addServices(sd);
		
		//to register the service
		registerService(dfd);
		
	
		 addBehaviour(new CyclicBehaviour() {
			
			@Override
			public void action() {
		
				ACLMessage message =blockingReceive();
				if(message != null && message.getPerformative() == ACLMessage.CFP) {
					  price = books.get(message.getContent());
					
						
						String string=myAgent.getLocalName()+" says: price for "+message.getContent()+" is: "+price+"DZD";
						System.out.println(string);
						textPane.append(string+"\n");
						
						ACLMessage reply=message.createReply();
						reply.setContent(price+"");
						reply.setPerformative(ACLMessage.PROPOSE);
						send(reply);
				}else {
					if(message!= null && message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
						String string = "Buyer accepts proposal from "
										+myAgent.getLocalName()+" and says '"+message.getContent()+"'\n"
												+ "*******************************************\n\n";
						System.out.println(string);
						textPane.append(string);
						block();
						
					} 
					else { 
						if(message!= null 
								&& message.getPerformative() == ACLMessage.PROPOSE
									&& message.getConversationId() != null
										&& message.getConversationId().equalsIgnoreCase("negotiate")){
						
						String string = myAgent.getLocalName()+" "
								+ "receives message from the buyer saying: "+message.getContent()+"\n";
						textPane.append(string);

						ACLMessage reply=message.createReply();
						reply.setContent( (price - 30) +"");
						reply.setPerformative(ACLMessage.PROPOSE);
						String text=getLocalName()+" proposes to "+message.getSender().getLocalName()
																+" the price "+(price-30)+"DZD\n"; 
						textPane.append(text);
						send(reply);
						block();
					
						}
					}
				
				}
			}
			
		});
		
	}
	
	
	public void registerService(DFAgentDescription dfd) {
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	
	public void initBooks() {
		books.put("XML", (int)(Math.random()* 100) + 50);
		books.put("JAVA", (int)(Math.random()* 100) + 90);
		books.put("UBUNTU", (int)(Math.random()* 100) + 80);
		books.put("WINDOWS", (int)(Math.random()* 100) + 100);
		books.put("LARAVEL", (int)(Math.random()* 100) + 250);
	}

	@Override
	protected void takeDown() {

		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
}
