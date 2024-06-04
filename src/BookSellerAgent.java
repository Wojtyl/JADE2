package jadelab2;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;
import java.util.stream.Collectors;

public class BookSellerAgent extends Agent {
  private Hashtable<String, BookDetails> catalogue;
  private BookSellerGui myGui;

  protected void setup() {
    catalogue = new Hashtable();
    myGui = new BookSellerGui(this);
    myGui.display();

    //book selling service registration at DF
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType("book-selling");
    sd.setName("JADE-book-trading");
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    }
    catch (FIPAException fe) {
      fe.printStackTrace();
    }

    addBehaviour(new OfferRequestsServer());

    addBehaviour(new PurchaseOrdersServer());
  }

  protected void takeDown() {
    //book selling service deregistration at DF
    try {
      DFService.deregister(this);
    }
    catch (FIPAException fe) {
      fe.printStackTrace();
    }
  	myGui.dispose();
    System.out.println("Seller agent " + getAID().getName() + " terminated.");
  }

  //invoked from GUI, when a new book is added to the catalogue

  public void updateCatalogue(final String title, final int price, final int shippingPrice) {
    addBehaviour(new OneShotBehaviour() {
      public void action() {
		catalogue.put(title, new BookDetails(title, price, shippingPrice));
		System.out.println(getAID().getLocalName() + ": " + title
				+ " put into the catalogue. Price = " + price
				+ " Shipping Price = " + shippingPrice
				+ " Total Price = " + (price + shippingPrice));
      }
    } );
  }

//  public List<BookDetails> getBooksFromCatalogueByTitle(String title) {
//	  return catalogue.stream().filter(x -> Objects.equals(x.title, title)).collect(Collectors.toList());
//  }

//  public BookDetails getCheapestNotReservedBook(List<BookDetails> books) {
//	  if (books.isEmpty()) {
//		  return null;
//	  } else {
//		  books.sort((x, y) -> (x.price + x.shippingPrice) <= (y.price + y.shippingPrice) ? -1 : 1);
//		  return books.get(0);
//	  }
//  }

	private class OfferRequestsServer extends CyclicBehaviour {
	  public void action() {
	    //proposals only template

		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = myAgent.receive(mt);
	    if (Objects.equals(getAID().getLocalName(), "seller2")) {
		    return;
	    }
	    if (msg != null) {
	      String title = msg.getContent();
	      ACLMessage reply = msg.createReply();
	      BookDetails bookDetails = catalogue.get(title);
			System.out.println(bookDetails);
	      if (bookDetails != null) {
			  System.out.println(bookDetails.reserved);
	        //title found in the catalogue, respond with its price as a proposal
			  if (!bookDetails.reserved) {
				reply.setPerformative(ACLMessage.PROPOSE);
				catalogue.get(title).reserved = true;
				reply.setContent(String.valueOf(bookDetails.price + bookDetails.shippingPrice));
			  } else {
				  reply.setPerformative(ACLMessage.DISCONFIRM);
				  reply.setContent("reserved");
			  }
	      } else {
	        //title not found in the catalogue
	        reply.setPerformative(ACLMessage.REFUSE);
	        reply.setContent("not-available");
	      }
	      myAgent.send(reply);
	    }
	    else {
	      block();
	    }
	  }
	}


	private class PurchaseOrdersServer extends CyclicBehaviour {
	  public void action() {
	    //purchase order as proposal acceptance only template
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage msg = myAgent.receive(mt);
//	    if (Objects.equals(getAID().getLocalName(), "seller2")) {
//		    System.out.println(getAID().getLocalName() + " Deciding not to respond to CFP from " + msg.getSender().getLocalName());
//		    return;
//	    }
	    if (msg != null) {
	      String title = msg.getContent();
	      ACLMessage reply = msg.createReply();
	      BookDetails bookDetails = catalogue.remove(title);
	      if (bookDetails != null) {
	        reply.setPerformative(ACLMessage.INFORM);
	        System.out.println(getAID().getLocalName() + ": " + title
					+ " sold to " + msg.getSender().getLocalName()
					+ ". Book price: " + (bookDetails.price + bookDetails.shippingPrice));
	      }
	      else {
	        //title not found in the catalogue, sold to another agent in the meantime (after proposal submission)
	        reply.setPerformative(ACLMessage.FAILURE);
	        reply.setContent("not-available");
	      }
	      myAgent.send(reply);
	    }
	    else {
		  block();
		}
	  }
	}

}
