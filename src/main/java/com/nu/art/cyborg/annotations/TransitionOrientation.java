package com.nu.art.cyborg.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.nu.art.cyborg.core.animations.transitions.BaseTransition.ORIENTATION_HORIZONTAL;
import static com.nu.art.cyborg.core.animations.transitions.BaseTransition.ORIENTATION_VERTICAL;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Documented
@Retention(SOURCE)
@Target( {
	         PARAMETER,
	         FIELD,
	         LOCAL_VARIABLE
         })
@IntDef( {
	         ORIENTATION_HORIZONTAL,
	         ORIENTATION_VERTICAL
         })
public @interface TransitionOrientation {}
