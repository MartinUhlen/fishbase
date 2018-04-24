package se.martinuhlen.fishbase.dao;

import static java.lang.Thread.currentThread;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;

import se.martinuhlen.fishbase.domain.AutoCompleteField;
import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

public interface FishBaseDao
{
	public static FishBaseDao create(Persistence persistence)
	{
	    CompletableFuture<JsonDao> futureDao = supplyAsync(() -> new JsonDao(persistence));
	    return (FishBaseDao) newProxyInstance(currentThread().getContextClassLoader(), new Class<?>[] {FishBaseDao.class}, (proxy, method, args) ->
	    {
	        JsonDao dao = futureDao.get();
	        return method.invoke(dao, args);
	    });
	}

	List<Specie> getSpecies();

	void saveSpecies(Collection<Specie> species);

	boolean isSpecieDeletable(Specie specie);

	void deleteSpecies(Collection<Specie> species);

	List<Specimen> getSpecimens();

	void saveSpecimens(Collection<Specimen> specimens);

	void deleteSpecimens(Collection<Specimen> specimens);

	List<Trip> getTrips();

	Trip getTrip(String id);

	void saveTrip(Trip trip);

	void deleteTrip(Trip wrapee);

	SortedSet<String> getAutoCompletions(AutoCompleteField field);
}
