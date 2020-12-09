package com.mie.dao;

import com.mie.model.Filter;
import com.mie.model.Product;
import com.mie.model.User;
import com.mie.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductDao {

	//TODO: complete empty methods
	
	private Connection connection;
	
	public ProductDao() {
		connection = DbUtil.getConnection();
	}
	
	public void addProduct(Product product) {
		try {
			PreparedStatement preparedStatement1 = connection
					.prepareStatement("insert into products(title,category,brand,fragranceFamily,price,occasion,personality,imageLink) values (?, ?, ?, ?, ?,?,?,? )");
			preparedStatement1.setString(1, product.getTitle());
			preparedStatement1.setString(2, product.getCategory());
			preparedStatement1.setString(3, product.getBrand());
			preparedStatement1.setString(4, product.getFragranceFamily());
			preparedStatement1.setFloat(5, product.getPrice());
			preparedStatement1.setString(6, product.getOccasion());
			preparedStatement1.setString(7, product.getPersonality());
			preparedStatement1.setString(8, product.getImageLink());
			boolean result1 = preparedStatement1.execute();
			
			//read prodid of the product that was just added
			String query = "SELECT prodId FROM Products WHERE title = ? AND category = ? AND brand = ? AND fragranceFamily = ? AND price = ? AND occasion = ? AND personality = ? AND imageLink = ?";
			PreparedStatement preparedStatement4 = connection.prepareStatement(query);
			preparedStatement4.setString(1, product.getTitle());
			preparedStatement4.setString(2, product.getCategory());
			preparedStatement4.setString(3, product.getBrand());
			preparedStatement4.setString(4, product.getFragranceFamily());
			preparedStatement4.setFloat(5, product.getPrice());
			preparedStatement1.setString(6, product.getOccasion());
			preparedStatement1.setString(7, product.getPersonality());
			preparedStatement1.setString(8, product.getImageLink());
			ResultSet rs = preparedStatement4.executeQuery();
			
			if (result1) {
				List<String> links = product.getLinks();
				int i = 0;
				while (i < links.size()) {
					PreparedStatement preparedStatement2 = connection
						.prepareStatement("insert into productlinks(link, prodId) values (?, ?)");
					preparedStatement2.setString(1, links.get(i));
					preparedStatement2.setInt(2, rs.getInt("prodId"));
					boolean result2 = preparedStatement2.execute();
				}
				
				List<String> notes = product.getNotes();
				int j = 0;
				while (j < notes.size()) {
					PreparedStatement preparedStatement3 = connection
						.prepareStatement("insert into productnotes(note, prodId) values (?, ?)");
					preparedStatement3.setString(1, notes.get(j));
					preparedStatement3.setInt(2, rs.getInt("prodId"));
					boolean result3 = preparedStatement3.execute();
				}
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Product getProduct(int prodId) {
		Product product = new Product();
		try {
			PreparedStatement preparedStatment = connection
					.prepareStatement("select * from products where prodid =?");
			preparedStatment.setInt(1, prodId);
			ResultSet rs = preparedStatment.executeQuery();
			
			//TODO: query productlinks and productnodes
			String query  = "select link from productlinks where prodid =?";
			PreparedStatement preparedStatment2 = connection.prepareStatement(query);
			preparedStatment2.setInt(1, prodId);
			ResultSet rs2 = preparedStatment.executeQuery();
			
			String query2  = "select note from productnotes where prodid =?";
			PreparedStatement preparedStatment3 = connection.prepareStatement(query);
			preparedStatment3.setInt(1, prodId);
			ResultSet rs3 = preparedStatment.executeQuery();
			
			while (rs.next()) {
				List<String> links = new ArrayList<String>();
				List<String> notes = new ArrayList<String>();
				product.setProdId(rs.getInt("prodid"));
				product.setTitle(rs.getString("title"));
				product.setCategory(rs.getString("category"));
				product.setBrand(rs.getString("brand"));
				product.setFragranceFamily(rs.getString("fragrancefamily"));
				product.setPrice(rs.getFloat("price"));
				product.setOccasion(rs.getString("occasion"));
				product.setPersonality(rs.getString("personality"));
				product.setImageLink(rs.getString("imageLink"));
				
				while (rs2.next()) {
					links.add(rs2.getString("links"));
				}
				product.setLinks(links);
				while (rs3.next()) {
					notes.add(rs3.getString("notes"));
				}
				product.setLinks(notes);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} return product;
	}
	
	public List<Product> getProductByKeyword(String keyword) {
		List<Product> productList = new ArrayList<Product>();
		List<Product> productByKeyword = new ArrayList<Product>();
		ProductDao allProducts = new ProductDao();
		
		productList.addAll(allProducts.getAllProducts());
		
		for (Product product : productList) {
			if (product.getTitle().equalsIgnoreCase(keyword)) {
				productByKeyword.add(product);		
			}
			if (product.getCategory().equalsIgnoreCase(keyword)) {
				if (productByKeyword.contains(product) == false) {
					productByKeyword.add(product);
				}
			}
			if (product.getBrand().equalsIgnoreCase(keyword)) {
				if (productByKeyword.contains(product) == false) {
					productByKeyword.add(product);
				}
			}
			if (product.getFragranceFamily().equalsIgnoreCase(keyword)) {
				if (productByKeyword.contains(product) == false) {
					productByKeyword.add(product);
				}
			}
			for (String notes : product.getNotes()) {
				if (notes.equalsIgnoreCase(keyword)) {
					if (productByKeyword.contains(product) == false) {
						productByKeyword.add(product);
					}
				}
			}
		}
		
		return productByKeyword;
	}
	
	public List<Product> filterProducts(Filter filter) {
		List<Product> originalProductList = new ArrayList<Product>();
		List<Product> productList = new ArrayList<Product>();
		ProductDao allProducts = new ProductDao();
		
		originalProductList.addAll(allProducts.getAllProducts());
		
		if (filter.getBrand().get(0).equals("default") && filter.getNote().get(0).equals("default")) { //filter.getPriceRange().get(0) == -1
			return originalProductList;
		}
		else {
			for (Product product : originalProductList) {
				if (!filter.getBrand().get(0).equals("default")) {
					for (String brand : filter.getBrand()) {
						if (product.getBrand().equalsIgnoreCase(brand)) {
							productList.add(product);
						}
					}
				}
//				if (filter.getPriceRange().get(0) != -1) {
//					int max = Collections.max(filter.getPriceRange());
//					if (product.getPrice() <= max) {
//						if (!productList.contains(product)) {
//							productList.add(product);
//						}
//					}
//				}
				if (filter.getNote().get(0).equals("default")) {
					for (String note : filter.getNote()) {
						for (String prodNote : product.getNotes()) {
							if (prodNote.equalsIgnoreCase(note)) {
								if (!productList.contains(product)) {
									productList.add(product);
								}
							}
						}
					}
				}
			}
		}
		
		return productList;
	}
	
	public void updateProduct(Product product) {
		try {
			String query = "UPDATE Products SET title = ?, category = ?, brand = ?, fragrenceFamily = ?, price = ?, occasion = ?, personality = ?, imageLink = ? WHERE prodId = ?";
			PreparedStatement preparedStatment = connection.prepareStatement(query);
			preparedStatment.setString(1, product.getTitle());
			preparedStatment.setString(2, product.getCategory());
			preparedStatment.setString(3, product.getBrand());
			preparedStatment.setString(4, product.getFragranceFamily());
			preparedStatment.setFloat(5, product.getPrice());
			preparedStatment.setString(6, product.getOccasion());
			preparedStatment.setString(7, product.getPersonality());
			preparedStatment.setString(8, product.getImageLink());
			preparedStatment.setInt(9, product.getProdId());
			ResultSet rs = preparedStatment.executeQuery();
			
			//update ProductLinks table
			//delete original links associated with prodId
			String query2 = "DELETE FROM ProductLinks WHERE prodId = ?";
			PreparedStatement preparedStatment2 = connection.prepareStatement(query2);
			preparedStatment.setInt(1, product.getProdId());
			int rs2 = preparedStatment2.executeUpdate();
			
			//insert new links
			List<String> linkList = product.getLinks();
			String query3 = "INSERT INTO ProductLinks(link, prodId) values (?, ?)";
			PreparedStatement preparedStatement3 = connection.prepareStatement(query3);
			for (String link : linkList) {
				preparedStatment.setString(1, link);
				preparedStatment.setInt(2, product.getProdId());
			}
			boolean rs3 = preparedStatement3.execute();
			
			//update ProductNotes table
			//delete original links associated with prodId
			String query4 = "DELETE FROM ProductNotes WHERE prodId = ?";
			PreparedStatement preparedStatment4 = connection.prepareStatement(query4);
			preparedStatment4.setInt(1, product.getProdId());
			int rs4 = preparedStatment4.executeUpdate();
			
			//insert new notes
			List<String> noteList = product.getNotes();
			String query5 = "INSERT INTO ProductNotes(note, prodId) values (?, ?)";
			PreparedStatement preparedStatement5 = connection.prepareStatement(query5);
			for (String note : noteList) {
				preparedStatment.setString(1, note);
				preparedStatment.setInt(2, product.getProdId());
			}
			boolean rs5 = preparedStatement5.execute();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	public void deleteProduct(int prodId) {
		try {
			String query = "DELETE FROM Products WHERE prodId = ?";
			PreparedStatement preparedStatment = connection.prepareStatement(query);
			preparedStatment.setInt(1, prodId);
			int rs = preparedStatment.executeUpdate();
			
			String query2 = "DELETE FROM ProductLinks WHERE prodId = ?";
			PreparedStatement preparedStatment2 = connection.prepareStatement(query2);
			preparedStatment2.setInt(1, prodId);
			int rs2 = preparedStatment2.executeUpdate();
			
			String query3 = "DELETE FROM ProductNotes WHERE prodId = ?";
			PreparedStatement preparedStatment3 = connection.prepareStatement(query3);
			preparedStatment3.setInt(1, prodId);
			int rs3 = preparedStatment3.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
		
	public List<Product> getAllProducts() {
		List<Product> productList = new ArrayList<Product>();
		
		try {
			PreparedStatement preparedStatment = connection
					.prepareStatement("select * from Products");
			ResultSet rs = preparedStatment.executeQuery();
			
			//TODO: query productlinks and productnodes
			String query = "SELECT P.prodId, L.link FROM ProductLinks L INNER JOIN Products P ON L.prodId = P.prodId";
			PreparedStatement preparedStatment2 = connection.prepareStatement(query);
			ResultSet rs2 = preparedStatment2.executeQuery();
			
			String query2 = "SELECT P.prodId, N.note FROM ProductNotes N INNER JOIN Products P ON N.prodId = P.prodId";
			PreparedStatement preparedStatment3 = connection.prepareStatement(query2);
			ResultSet rs3 = preparedStatment3.executeQuery();
			
			while (rs.next()) {
				Product product = new Product();
				List<String> links = new ArrayList<String>();
				List<String> notes = new ArrayList<String>();
				product.setProdId(rs.getInt("prodid"));
				product.setTitle(rs.getString("title"));
				product.setCategory(rs.getString("category"));
				product.setBrand(rs.getString("brand"));
				product.setFragranceFamily(rs.getString("fragrancefamily"));
				product.setPrice(rs.getFloat("price"));
				product.setOccasion(rs.getString("occasion"));
				product.setPersonality(rs.getString("personality"));
				product.setImageLink(rs.getString("imageLink"));
				 
				while (rs2.next()) {
					if (rs.getInt("prodid") == rs2.getInt("prodid")) {
						links.add(rs2.getString("link"));
					}	
				}
				product.setLinks(links);
				links.clear();
				while (rs3.next()) {
					if (rs.getInt("prodid") == rs3.getInt("prodid")) {
						notes.add(rs3.getString("note"));
					}	
				}
				product.setNotes(notes);
				notes.clear();
				productList.add(product);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return productList;
	}
}
