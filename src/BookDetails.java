package jadelab2;

//[1]: Added Book details class to store all book information in one class
public class BookDetails {
	int price;
	int shippingPrice;
	String title;

	public BookDetails(String title, int price, int shippingPrice){
		this.price = price;
		this.shippingPrice = shippingPrice;
		this.title = title;
	}
}
