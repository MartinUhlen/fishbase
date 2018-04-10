package se.martinuhlen.fishbase.dao;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import se.martinuhlen.fishbase.domain.Specie;
import se.martinuhlen.fishbase.domain.Specimen;
import se.martinuhlen.fishbase.domain.Trip;

public interface FishBaseDao
{
	public static FishBaseDao create(Persistence persistence)
	{
	    CompletableFuture<JsonDao> futureDao = CompletableFuture.supplyAsync(() -> new JsonDao(persistence));
	    return (FishBaseDao) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {FishBaseDao.class}, (Object proxy, Method method, Object[] args) ->
	    {
	        JsonDao dao = futureDao.get();
	        return method.invoke(dao, args);
	    });
	}

	List<Specie> getSpecies();

	Specie getSpecie(String id);

	void saveSpecie(Specie specie);

	void saveSpecies(Collection<? extends Specie> species);

	boolean isSpecieDeletable(Specie specie);

	void deleteSpecies(Collection<? extends Specie> species);

	List<Specimen> getSpecimens();

	Specimen getSpecimen(String id);

	void saveSpecimen(Specimen specimen);

	void saveSpecimens(Collection<? extends Specimen> specimens);

	void deleteSpecimens(Collection<? extends Specimen> specimens);

	List<Trip> getTrips();

	Trip getTrip(String id);

	void saveTrip(Trip trip);

	void deleteTrip(Trip wrapee);
}
