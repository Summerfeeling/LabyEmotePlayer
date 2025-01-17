package de.summerfeeling.labyemoteplayer.utils.reflect;

import java.lang.reflect.Method;

public interface MethodAccessor {

	<T> T invoke(Object instance, Object... args);
	Method getMethod();
}
