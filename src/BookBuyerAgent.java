package jadelab2;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class BookBuyerAgent extends Agent {
  private BookBuyerGui myGui;
  private BookBuyerBudgetGui myBudgetGui;
  private String targetBookTitle;

  // [1]: Add budget field and budget getter
  private int budget;
  public int getBudget() {
	  return this.budget;
  }

  //list of found sellers
  private AID[] sellerAgents;
  
	protected void setup() {
	  targetBookTitle = "";

	  // [2]: Init default buyer budget with init budget info
	  budget = 1500;

	  System.out.println("Hello! " + getAID().getLocalName() + " is ready for the purchase order.");
	  System.out.println(getAID().getLocalName() + " budget is " + budget + "$");
	  myGui = new BookBuyerGui(this);
	  // [3]: New UI for adding budget when is too low
	  myBudgetGui = new BookBuyerBudgetGui(this);
	  myGui.display();
		//time interval for buyer for sending subsequent CFP
		//as a CLI argument
		int interval = 20000;
		Object[] args = getArguments();
		if (args != null && args.length > 0) interval = Integer.parseInt(args[0].toString());
	  addBehaviour(new TickerBehaviour(this, interval)
	  {
		  protected void onTick()
		  {
			  //search only if the purchase task was ordered
			  if (!targetBookTitle.equals(""))
			  {
				  System.out.println(getAID().getLocalName() + ": I'm looking for " + targetBookTitle);
				  //update a list of known sellers (DF)
				  DFAgentDescription template = new DFAgentDescription();
				  ServiceDescription sd = new ServiceDescription();
				  sd.setType("book-selling");
				  template.addServices(sd);
				  try
				  {
					  DFAgentDescription[] result = DFService.search(myAgent, template);
					  System.out.println(getAID().getLocalName() + ": the following sellers have been found");
					  sellerAgents = new AID[result.length];
					  for (int i = 0; i < result.length; ++i)
					  {
						  sellerAgents[i] = result[i].getName();
						  System.out.println(sellerAgents[i].getLocalName());
					  }
				  }
				  catch (FIPAException fe)
				  {
					  fe.printStackTrace();
				  }

				  myAgent.addBehaviour(new RequestPerformer());
			  }
		  }
	  });
  }

	//invoked from GUI, when purchase was ordered
	public void lookForTitle(final String title)
	{
		addBehaviour(new OneShotBehaviour()
		{
			public void action()
			{
				targetBookTitle = title;
				System.out.println(getAID().getLocalName() + ": purchase order for " + targetBookTitle + " accepted");
			}
		});
	}

    	protected void takeDown() {
		myGui.dispose();
		System.out.println("Buyer agent " + getAID().getLocalName() + " terminated.");
	}
	// [4]: Method to add money to current budget
	public void addMoney(int amount) {
		this.budget += amount;
		System.out.println(getAID().getLocalName() + ": " + amount + "$ was added to account. Current budget is: " + getBudget() + "$");
	}

	private class RequestPerformer extends Behaviour {
	  private AID bestSeller;
	  private int bestPrice;
	  private int repliesCnt = 0;
	  private MessageTemplate mt;
	  private int step = 0;
	
	  public void action() {
	    switch (step) {
	    case 0:
	      //call for proposal (CFP) to found sellers
	      ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
	      for (int i = 0; i < sellerAgents.length; ++i) {
	        cfp.addReceiver(sellerAgents[i]);
	      } 
	      cfp.setContent(targetBookTitle);
	      cfp.setConversationId("book-trade");
	      cfp.setReplyWith("cfp"+System.currentTimeMillis()); //unique value
	      myAgent.send(cfp);
	      mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
	                               MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
	      step = 1;
	      break;
	    case 1:
	      //collect proposals
	      ACLMessage reply = myAgent.receive(mt);
	      if (reply != null) {
	        if (reply.getPerformative() == ACLMessage.PROPOSE) {
	          //proposal received
	          int price = Integer.parseInt(reply.getContent());
	          if (bestSeller == null || price < bestPrice) {
	            //the best proposal as for now
	            bestPrice = price;
	            bestSeller = reply.getSender();
	          }
	        }
	        repliesCnt++;
	        if (repliesCnt >= sellerAgents.length) {
	          //all proposals have been received
	          step = 2; 
	        }
	      }
	      else {
	        block();
	      }
	      break;
	    case 2:
	      //best proposal consumption - purchase
			// [5]: Logic to detect if budget is too low and stop ordering process and display budget UI to add money to budget
			if (budget < bestPrice) {
				System.out.println(getAID().getLocalName() + ": budget is too low! " +
						"Cheapest book price is " + bestPrice + "$. " +
						"Buyer budget is " + budget + "$");
				targetBookTitle = "";
				myBudgetGui.display();
				step = 4;
				break;
			} else {
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				order.setContent(targetBookTitle);
				order.setConversationId("book-trade");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			}
	    case 3:
	      //seller confirms the transaction
	      reply = myAgent.receive(mt);
	      if (reply != null) {
	        if (reply.getPerformative() == ACLMessage.INFORM) {
	          //purchase succeeded
				// [6]: Decrease budget amount and show info about current budget
			  budget -= bestPrice;
	          System.out.println(getAID().getLocalName() + ": " + targetBookTitle + " purchased for " + bestPrice + " from " + reply.getSender().getLocalName());
		  	  System.out.println(getAID().getLocalName() + ": waiting for the next purchase order." + " Buyer budget is " + budget + "$");
			  targetBookTitle = "";
	          //myAgent.doDelete();
	        }
	        else {
	          System.out.println(getAID().getLocalName() + ": purchase has failed. " + targetBookTitle + " was sold in the meantime.");
	        }
	        step = 4;	//this state ends the purchase process
	      }
	      else {
	        block();
	      }
	      break;
	    }        
	  }
	
	  public boolean done() {
	  	if (step == 2 && bestSeller == null) {
	  		System.out.println(getAID().getLocalName() + ": " + targetBookTitle + " is not on sale.");
	  	}
	    //process terminates here if purchase has failed (title not on sale) or book was successfully bought 
	    return ((step == 2 && bestSeller == null) || step == 4);
	  }
	}

}
