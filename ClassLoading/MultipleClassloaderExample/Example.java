import java.net.URL;
import java.util.List;


/*
 * For more interesting details please refer to this answer:
 *   http://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader-in-java-or-how-to-overr
 *
 * This is a simple implementation of class loader which tries to find class in his classpath FIRST.
 * Default behavior is PARENT FIRST.
 */
class ChildFirstClassLoader extends java.net.URLClassLoader {

	private ClassLoader realParent;

	public ChildFirstClassLoader(URL[] classpath) {
		super(classpath, null);

		this.realParent = Thread.currentThread().getContextClassLoader();
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			System.out.println("Tried to use custom class loader! " + name);
			return super.findClass(name);
		}
		catch(ClassNotFoundException e ) {
			System.out.println("But failed miserably!");
			return realParent.loadClass(name);
		}
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return findClass(name);
	}
}


class ToBeLoaded {
	static {
		System.out.println("Loaded!");
	}
}


class MyThread extends Thread {

	public MyThread() {
		super();
	}

	@Override
	public void run() {
		try {
			new ToBeLoaded();
			System.out.println("I am making a note here. Huge Success!");
			System.out.println(MyThread.class.getClassLoader());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}


public class Example {

	public static void main(String args[]) throws Exception {
		URL[] classpath = new URL[]{new java.io.File(".").toURI().toURL()};
		ChildFirstClassLoader parentLastURLClassLoader = new ChildFirstClassLoader(classpath);

		Class clazz = Class.forName("MyThread", true, parentLastURLClassLoader);

		java.lang.reflect.Constructor constructor = clazz.getDeclaredConstructor(new Class[]{});

		// For some reason non-public class constructor are considered to be unreachable.
		// More details: http://stackoverflow.com/questions/23180476/public-constructor-of-a-private-class
		constructor.setAccessible(true);

		Object myThread = constructor.newInstance();

		java.lang.reflect.Method method = myThread.getClass().getMethod("start", new Class[] {});
		method.invoke(myThread, new Object[] {});
	}

}