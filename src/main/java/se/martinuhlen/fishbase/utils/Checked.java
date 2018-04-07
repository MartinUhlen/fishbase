package se.martinuhlen.fishbase.utils;

public class Checked
{
	public interface Runnable
	{
		void run() throws Exception;
	}

	public static java.lang.Runnable $(Runnable runnable)
	{
		return () ->
		{
			run(runnable);
		};
	}

	public static void run(Runnable runnable)
	{
		try
		{
			runnable.run();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public interface Supplier<T>
	{
	    T get() throws Exception;
	}

	public static <T> java.util.function.Supplier<T> $(Supplier<T> supplier)
	{
		return () ->
		{
			return get(supplier);
		};
	}

	public static <T> T get(Supplier<T> supplier)
	{
		try
		{
			return supplier.get();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public interface Consumer<T>
	{
		void accept(T t) throws Exception;
	}

	public static <T> java.util.function.Consumer<T> $(Consumer<T> consumer)
	{
		return t ->
		{
			try
			{
				consumer.accept(t);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		};
	}

//	public interface Function<T, R>
//	{
//	    R apply(T t) throws Exception;
//	}
//
//	public static <T, R> java.util.function.Function<T, R> $(Function<T, R> function)
//	{
//		return t ->
//		{
//			try
//			{
//				return function.apply(t);
//			}
//			catch (Exception e)
//			{
//				throw new RuntimeException(e);
//			}
//		};
//	}
}
