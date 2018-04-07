package se.martinuhlen.fishbase;

import javafx.application.Application;
import se.martinuhlen.fishbase.javafx.FishBaseApplication;

public class FishBase
{
	public static void main(String[] args)
	{
		try
		{
			//Injector injector = Guice.createInjector(new DaoModule());
			Application.launch(FishBaseApplication.class, args);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
		finally
		{
			System.out.println("Terminating...");
		}
	}
}
