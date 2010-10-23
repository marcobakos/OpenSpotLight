package org.openspotlight.bundle.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyDef {
	String name();

	boolean mandatory() default false;

//	PropertyDefType type();
//
//	public enum PropertyDefType {
//		STRING, INT, FLOAT, BOOLEAN, PATH, CONTEXT_REF, CONTEXT_LIST_REF
//	}

}