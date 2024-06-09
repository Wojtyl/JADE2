package jadelab2;

public class BookDetails {
	int price;
	int shippingPrice;
	String title;
	Long reserved;

	public BookDetails(String title, int price, int shippingPrice){
		this.price = price;
		this.shippingPrice = shippingPrice;
		this.title = title;
		this.reserved = null;
	}
}
