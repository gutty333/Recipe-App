package com.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.connection.DatabaseConnection;

public class Menu
{
	private static Scanner input = new Scanner(System.in);
	private static int choice;
	private static String line = "----------------------------";
	private static Connection myConnection;
	private static PreparedStatement statement;
	private static ResultSet resultSet;
	
	public static void start()
	{
		try
		{
			// connecting to our database
			myConnection = DatabaseConnection.getConnection();
			
			// navigating to the home screen
			homeMenu();
		}
		catch (SQLException e) 
		{
			System.out.println(e.getMessage());
		}
		finally 
		{
			// closing our connection
			try
			{
				myConnection.close();
				
				if (statement != null)
				{
					statement.close();
				}
				
				if (resultSet != null)
				{
					resultSet.close();
				}
			} 
			catch (SQLException e)
			{
				System.out.println(e.getMessage());
			}
		}
	}
	
	// method displaying the main menu screen
	public static void homeMenu() throws SQLException
	{
		do
		{
			// menu options
			System.out.print("\nHome Menu\n" + line + "\n");
			System.out.println("Please select an option:");
			System.out.println("1. Search for Recipes");
			System.out.println("2. Add Recipes");
			System.out.println("3. View Recommened Recipes");
			System.out.println("4. Exit Application");
			
			// getting user input
			choice = input.nextInt();
			
			switch (choice)
			{
				case 1:
				{
					// navigate to search menu
					searchMenu();
					break;
				}
				case 2:
				{
					// navigate to add menu
					addMenu();
					break;
				}
				case 3:
				{
					// navigate to recommended menu
					recommendedMenu();
					break;
				}
				case 4:
				{
					System.out.println("Thank you for using this service");
					break;
				}
				default:
				{
					System.out.println("Invalid choice entry, please provide a value of 1-4\n");
				}
			}
		} while ((choice < 1 || choice > 4) || choice != 4);
	}
	
	// method for adding a new recipe to our database
	public static void addMenu() throws SQLException
	{
		if (input.hasNextLine())
		{
			input.nextLine();
		}
		
		// variables for our new record
		String author;
		String recipeName;
		String description;
		double cost;
		int calories;
		int prepTime;
		List<String> ingredients = new ArrayList<>();

		// Author
		System.out.println("\nPlease provide the following information.");
		System.out.println("Enter your name:");
		author = input.nextLine();
		
		// Recipe Name
		System.out.println("Enter the recipe name:");
		recipeName = input.nextLine();
		
		// Description
		System.out.println("Enter the recipe description:");
		description = input.nextLine();
		
		// Estimated Cost
		System.out.println("Enter the estimated cost:");
		cost = input.nextDouble();
		
		// Estimated Calories
		System.out.println("Enter the estimated calories:");
		calories = input.nextInt();
		
		// Estimated Preparation Time
		System.out.println("Enter the estimated preparation time in minutes:");
		prepTime = input.nextInt();
		
		// Ingredients
		System.out.println("How many ingredients are needed for this recipe?");
		int size = input.nextInt();
		
		if (input.hasNextLine())
		{
			input.nextLine();
		}
		for (int x = 0; x < size; x++)
		{
			System.out.println("\nAdd ingredient #" +(x+1)+":");
			ingredients.add(input.nextLine());
		}

		// Confirmation
		System.out.println("\nAre you sure you want to publish a recipe for " + recipeName + "?(Y/N)");
		char confirm = input.next().charAt(0);
		
		if (confirm == 'Y')
		{
			// creating our insert query for our recipe
			String query = "INSERT INTO recipe (`Name`, `Author`, `Description`, `Cost`, `PrepTime`, `Calories`) "
					+ "VALUES (?, ?, ?, ?, ?, ?)";
			
			// creating our statement
			statement = myConnection.prepareStatement(query);
			
			// binding the parameters
			statement.setString(1, recipeName);
			statement.setString(2, author);
			statement.setString(3, description);
			statement.setDouble(4, cost);
			statement.setInt(5, prepTime);
			statement.setInt(6, calories);
			
			// executing the recipe statement
			if (statement.executeUpdate() == 1)
			{
				System.out.println(recipeName + " was succesfully published");
			}
			else
			{
				System.out.println(recipeName + " ERROR");
			}
			
			// ingredient section
			if (ingredients.size() > 0)
			{
				// statement for adding the ingredients, note we ignore any duplicates
				query = "INSERT IGNORE INTO ingredient (`Name`) VALUES (?)";
				statement = myConnection.prepareStatement(query);
				
				for (String current: ingredients)
				{
					statement.setString(1, current);
					statement.addBatch();
				}
				
				// executing the ingredients statement
				if (statement.executeBatch().length > 0)
				{
					System.out.println("Ingredients were succesfully published");
				}
				else
				{
					System.out.println("Ingredient ERROR");
				}
				
				// section to connect the ingredients to the recipe
				// creating pair connection
				query = "select ID from recipe where Name = ?";
				statement = myConnection.prepareStatement(query);
				statement.setString(1, recipeName);
				resultSet = statement.executeQuery();
				
				// getting the recipe ID
				resultSet.first();
				int recipeID = resultSet.getInt("ID");
				
				// getting the ingredients ID
				query = "select ID from ingredient where Name = ?";
				statement = myConnection.prepareStatement(query);
				List<Integer> ingredientsID = new ArrayList<>();
				
				// storing the ID of each ingredient related to this recipe
				for (String current: ingredients)
				{
					statement.setString(1, current);
					resultSet = statement.executeQuery();
					resultSet.first();
					ingredientsID.add(resultSet.getInt("ID"));
				}
				
				// Inserting the new recipe ingredient pair record
				query = "INSERT INTO recipe_ingredient (`RecipeID`, `IngredientID`) VALUES (?, ?)";
				statement = myConnection.prepareStatement(query);
				for (Integer current : ingredientsID)
				{
					statement.setInt(1, recipeID);
					statement.setInt(2, current);
					statement.addBatch();
				}
				
				if (statement.executeBatch().length > 0)
				{
					System.out.println("Recipe/Ingredients pair Successfully Published");
				}
				else
				{
					System.out.println("Recipe/Ingredients pair ERROR");
				}
			}
		}
	}
	
	// method allowing the user to search for recipes
	public static void searchMenu() throws SQLException
	{		
		do
		{
			// menu options
			System.out.print("\nSearch Menu\n" + line + "\n");
			System.out.println("Please select an option:");
			System.out.println("1. Search by Name");
			System.out.println("2. Search by Ingredient");
			System.out.println("3. Lowest Cost");
			System.out.println("4. Highest Cost");
			System.out.println("5. Lowest Calorie");
			System.out.println("6. Highest Calorie");
			System.out.println("7. Recent");
			System.out.println("8. Oldest");
			System.out.println("9. Best Rating");
			System.out.println("10. Exit");
			
			// getting user input
			choice = input.nextInt();
			
			switch (choice)
			{
				case 1:
				{
					// option to search by name
					nameSearch();
					break;
				}
				case 2:
				{
					// option to search by ingredient 
					ingredientSearch();
					break;
				}
				case 3:
				{
					// sorting from lowest to highest by cost
					sortAscendingCost();
					break;
				}
				case 4:
				{
					// sorting from highest to lowest by cost
					sortDescendingCost();
					break;
				}
				case 5:
				{
					// sorting from lowest to highest by calorie
					sortAscendingCalorie();
					break;
				}
				case 6:
				{
					// sorting from highest to lowest by calorie
					sortDescendingCalorie();
					break;
				}
				case 7:
				{
					// showing recently published recipes
					recentRecipes();
					break;
				}
				case 8:
				{
					// showing oldest published recipes
					oldestRecipes();
					break;
				}
				case 9:
				{
					// showing highest rated recipes
					highestRated();
					break;
				}
				case 10:
				{
					// back to main menu
					break;
				}
				default:
				{
					System.out.println("Invalid choice entry, please provide a value of 1-10\n");
				}
			}
		} while (choice < 1 || choice > 10);
		
		choice = 1;
	}

	// method to search by recipe name
	private static void nameSearch() throws SQLException
	{
		// query to search by recipe name
		String query = "select * from recipe where Name like ?";
		
		// take name input from user
		System.out.print("Enter a Recipe Name: ");
		if (input.hasNextLine())
		{
			input.nextLine();
		}
		StringBuilder name = new StringBuilder();
		name.append("%");
		name.append(input.nextLine());
		name.append("%");
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// binding our parameter
		statement.setString(1, name.toString());
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}
	
	// method to search by ingredient name
	private static void ingredientSearch() throws SQLException
	{
		// query to search for recipes by ingredient name
		String query = "select * from recipe left outer join recipe_ingredient " + 
				"on recipe_ingredient.RecipeID = recipe.ID left outer join ingredient " + 
				"on ingredient.ID = recipe_ingredient.IngredientID where ingredient.Name like ? group by recipe.Name";
		
		// take ingredient name input from user
		System.out.print("Enter an Ingredient: ");
		if (input.hasNextLine())
		{
			input.nextLine();
		}
		StringBuilder name = new StringBuilder();
		name.append("%");
		name.append(input.nextLine());
		name.append("%");
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// binding our parameter
		statement.setString(1, name.toString());
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}

	// method to sort from lowest to highest cost
	private static void sortAscendingCost() throws SQLException
	{
		// query to sort from lowest to highest cost
		String query = "select * from recipe order by Cost";
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}
	
	// method to sort from highest to lowest cost
	private static void sortDescendingCost() throws SQLException
	{
		// query to sort from highest to lowest cost
		String query = "select * from recipe order by Cost desc";
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}
	
	// method to sort from lowest to highest calories
	private static void sortAscendingCalorie() throws SQLException
	{
		// query to sort from lowest to highest calories
		String query = "select * from recipe order by Calories";
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}
	
	// method to sort from highest to lowest calories
	private static void sortDescendingCalorie() throws SQLException
	{
		// query to sort from highest to lowest calories
		String query = "select * from recipe order by Calories desc";
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}
	
	// method to show the most recently published recipes
	private static void recentRecipes() throws SQLException
	{
		// query to show most recent
		String query = "select * from recipe order by Date desc";
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}
	
	// method to show the oldest published recipes
	private static void oldestRecipes() throws SQLException
	{
		// query to show the oldest
		String query = "select * from recipe order by Date";
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}
	
	// method to show the highest rated recipes
	private static void highestRated() throws SQLException
	{
		// query to show top rated recipes
		String query = "select * from recipe order by Rating desc";
		
		// creating our statement 
		statement = myConnection.prepareStatement(query);
		
		// receiving the result
		resultSet = statement.executeQuery();
		
		// displaying the records
		displayResultSet();
	}
	
	// method for displaying the resultSet after executing a query
	private static void displayResultSet() throws SQLException
	{
		// displaying the results
		if (!resultSet.next())
		{
			System.out.println("No Records where found");
		}
		else 
		{
			int count = 0;
			
			do
			{
				// reset the cursor
				resultSet.beforeFirst();
				
				// displaying our records
				System.out.println("\nSelect a Recipe:");
				while (resultSet.next())
				{
					count++;
					System.out.println(count + ". " + resultSet.getString("Name"));
				}
				choice = input.nextInt();
				
				// input validation
				if (choice < 1 || choice > count)
				{
					System.out.println("Invalid choice entry, please provide a value of 1-"+count);
					count = 0;
				}
				else
				{
					// displaying the information
					displayRecipeInfo();
				}
			} while (choice < 1 || choice > count);
		}
	}

	// method for displaying the selected recipe's informations
	private static void displayRecipeInfo() throws SQLException
	{
		// moving the cursor to the selected recipe
		resultSet.absolute(choice);
		
		// displaying selected recipe information
		System.out.print("\n"+resultSet.getString("Name")+"\n" + line + "\n");
		System.out.println("Name: " + resultSet.getString("Name"));
		System.out.println("Author: " + resultSet.getString("Author"));
		System.out.println("Date Published: " + resultSet.getDate("Date"));
		System.out.println("\nDescription:\n" + resultSet.getString("Description")+"\n");
		System.out.println("Cost: $" + resultSet.getDouble("Cost"));
		System.out.println("Calories: " + resultSet.getInt("Calories"));
		System.out.println("Preparation Time: " + resultSet.getInt("PrepTime") + " minutes");
		
		// calculating and displaying the rating
		int totalReviews = resultSet.getInt("TotalRate");
		int reviewScore = resultSet.getInt("ActualRate");
		double ratingResult = 0;
		if (totalReviews != 0)
		{
			ratingResult = (double)reviewScore/totalReviews;
		}
		System.out.printf("Rating: %.1f/5.0\n", ratingResult);
		
		// displaying the ingredients
		System.out.println("\nIngredients Needed:");
		int recipeID = resultSet.getInt("ID");
		
		// getting all the ingredients associated with this recipe
		String query  = "select * from ingredient left outer join recipe_ingredient " + 
				"on ingredient.ID = recipe_ingredient.IngredientID " + 
				"where recipe_ingredient.RecipeID = ?";
		statement = myConnection.prepareStatement(query);
		statement.setInt(1, recipeID);
		ResultSet resultSet2 = statement.executeQuery();
		while(resultSet2.next())
		{
			System.out.println(resultSet2.getString("Name"));
		}

		int choice2;
		do
		{
			// options on the selected recipe
			System.out.println("\nPlease select an option:");
			System.out.println("1. Save Recipe");
			System.out.println("2. Rate Recipe");
			System.out.println("3. Delete Recipe");
			System.out.println("4. Exit");
			choice2 = input.nextInt();
			
			switch (choice2)
			{
				case 1:
				{
					// saving to a file
					saveRecipe();
					break;
				}
				case 2:
				{
					// rating the recipe
					rateRecipe();
					break;
				}
				case 3:
				{
					// deleting the recipe
					deleteRecipe();
					break;
				}
				case 4:
				{
					// returning to main menu
					break;
				}
				default:
				{
					System.out.println("Invalid choice entry, please provide a value of 1-4\n");
				}
			}
		} while (choice2 < 1 || choice2 > 4);
	}

	// method for rating a recipe
	private static void rateRecipe() throws SQLException
	{
		int rateNum;
		
		do
		{
			System.out.println("\nPlease provide a rating score of 1-5:");
			rateNum = input.nextInt();
			
			if (rateNum < 1 || rateNum > 5)
			{
				System.out.println("Invalid rate, please provide a value of 1-5");
			}
			else
			{
				// update query
				String query = "update recipe SET ActualRate =?, TotalRate =?, Rating =? WHERE ID=?";
				statement = myConnection.prepareStatement(query);
				
				// binding the parameters
				int totalReviews = resultSet.getInt("TotalRate") + 1;
				int reviewScore = resultSet.getInt("ActualRate") + rateNum;
				double ratingResult = (double)reviewScore/totalReviews;
				
				statement.setInt(1, reviewScore);
				statement.setInt(2, totalReviews);
				statement.setDouble(3, ratingResult);
				statement.setInt(4, resultSet.getInt("ID"));
				
				// executing the statement
				if (statement.executeUpdate() == 1)
				{
					System.out.println("Review was succesful");
				}
				else
				{
					System.out.println("Review ERROR");
				}
			}
			
		} while (rateNum < 1 || rateNum > 5);
	}

	// method to save a recipe information to a file
	private static void saveRecipe() throws SQLException
	{
		System.out.println("Saving the Recipe: " + resultSet.getString("Name"));
		
		// user input on the filename
		System.out.println("Please provide a filename");
		if(input.hasNextLine())
		{
			input.nextLine();
		}
		String fileName = input.nextLine();

		// creating our file and saving the information
		try (PrintWriter output = new PrintWriter(new File(fileName)))
		{
			output.print("\n"+resultSet.getString("Name")+"\n" + line + "\n");
			output.println("Name: " + resultSet.getString("Name"));
			output.println("Author: " + resultSet.getString("Author"));
			output.println("Date Published: " + resultSet.getDate("Date"));
			output.println("\nDescription:\n" + resultSet.getString("Description")+"\n");
			output.println("Cost: $" + resultSet.getDouble("Cost"));
			output.println("Calories: " + resultSet.getInt("Calories"));
			output.println("Preparation Time: " + resultSet.getInt("PrepTime") + " minutes");
			
			// rating section
			int totalReviews = resultSet.getInt("TotalRate");
			int reviewScore = resultSet.getInt("ActualRate");
			double ratingResult = 0;
			if (totalReviews != 0)
			{
				ratingResult = (double)reviewScore/totalReviews;
			}
			output.printf("Rating: %.1f/5.0\n", ratingResult);
			
			// ingredients section
			output.println("\nIngredients Needed:");
			int recipeID = resultSet.getInt("ID");
			
			// getting all the ingredients associated with this recipe
			String query  = "select * from ingredient left outer join recipe_ingredient " + 
					"on ingredient.ID = recipe_ingredient.IngredientID " + 
					"where recipe_ingredient.RecipeID = ?";
			statement = myConnection.prepareStatement(query);
			statement.setInt(1, recipeID);
			ResultSet resultSet2 = statement.executeQuery();
			while(resultSet2.next())
			{
				output.println(resultSet2.getString("Name"));
			}
			
			System.out.println("File succesfully saved");
		} 
		catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	// method allowing the user to delete the selected recipe
	public static void deleteRecipe() throws SQLException
	{
		String query = "delete from recipe where ID = ?";
		
		statement = myConnection.prepareStatement(query);
		
		// getting the current ID
		int recipeID = resultSet.getInt("ID");
		statement.setInt(1, recipeID);
		
		// executing the statement
		if (statement.executeUpdate() == 1)
		{
			System.out.println("Recipe succesfully deleted");
		}
		else
		{
			System.out.println("Recipe delete ERROR");
		}
	}
	
	// recommended recipe method
	// work in progress
	public static void recommendedMenu() throws SQLException
	{
		// query to only show top rated recipes
		String query = "select * from recipe where Rating > 4.2 limit 10";
		
		statement = myConnection.prepareStatement(query);
		
		resultSet = statement.executeQuery();
		
		// displaying the result
		displayResultSet();
		
		choice = 1;
	}
}