package de.summerfeeling.labyemoteplayer.utils.reflect;

import java.lang.reflect.Constructor;

public interface ConstructorAccessor<T> {
	
	T newInstance(Object... args);
	Constructor<T> getConstructor();

}
