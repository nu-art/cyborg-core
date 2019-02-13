package com.nu.art.cyborg.annotations;

import android.view.View;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Documented
@Retention(SOURCE)
@Target( {
	         METHOD,
	         PARAMETER,
         })
@IntDef( {
	         View.VISIBLE,
	         View.INVISIBLE,
	         View.GONE,
         })
public @interface Visibility {
}
