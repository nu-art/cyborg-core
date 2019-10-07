/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nu.art.cyborg.errorMessages;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.cyborg.annotations.ItemType;
import com.nu.art.cyborg.common.consts.ViewListener;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.CyborgController;
import com.nu.art.cyborg.core.ItemRenderer;
import com.nu.art.cyborg.tools.ResourceType;
import com.nu.art.cyborg.tools.ReverseR_Module;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Created by TacB0sS on 02/06/2017.
 */

public class ExceptionGenerator {

	private ExceptionGenerator() {
		throw new BadImplementationException("Don't be naughty");
	}

	private static String getViewNameFromId(@IdRes int viewId) {
		return CyborgBuilder.getModule(ReverseR_Module.class).getName(ResourceType.Id, viewId);
	}

	private static String getLayoutNameFromId(@IdRes int viewId) {
		return CyborgBuilder.getModule(ReverseR_Module.class).getName(ResourceType.Layout, viewId);
	}

	public static BadImplementationException noValueForControllerClassNameSpecified() {
		return new BadImplementationException("Expected a controller class FQN, but attribute value was empty");
	}

	public static BadImplementationException invalidControllerClassNameSpecified(String className, ClassNotFoundException e) {
		return new BadImplementationException("Expected a controller class FQN, but the value did not match to a class:\n" //
			                                      + "value found: " + className + "\n" //
			                                      + "if this is an issue of refactoring, please go and favorite this feature request: " + ErrorLinks.ClassNameRefactoringNotAppliedToXML, e);
	}

	public static BadImplementationException didNotProvideLayoutIdOrCustomView(CyborgController cyborgController) {
		String controllerClassName = cyborgController.getClass().getSimpleName();
		String errorMessage = "MUST specify a valid layoutId in the controller constructor or override createCustomView method in controller " + controllerClassName + "!";
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerHaveSetViewIdButMemberIsNotAnArray(Field viewField) {
		String errorMessage = "Your annotation declares multiple views, but your member is NOT an array of Views!!" + fieldDescription(viewField);
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerDidNotSetViewIdForViewInjector(Field viewField) {
		String errorMessage = "You MUST set valid viewId id for" + fieldDescription(viewField);
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerSetViewIdForViewArrayInjector(Field viewField) {
		String errorMessage = "You MUST NOT set a viewId id for array of views in" + fieldDescription(viewField);
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerDidNotSetViewIdsForViewArrayInjector(Field viewField) {
		String errorMessage = "You MUST set valid viewIds id for " + fieldDescription(viewField);
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerSetViewIdentifierAnnotationToMemberWithUnsupportedType(Field viewField) {
		String fieldName = viewField.getName();
		String classSimpleName = viewField.getDeclaringClass().getSimpleName();
		String errorMessage = "ViewIdentifier Annotation was set to an unsupported type: " + fieldName + " in class, '" + classSimpleName;
		errorMessage += "\n Annotation can only be set to a member assignable from View, View[], CyborgController, CyborgController[]!";
		return new BadImplementationException(errorMessage);
	}

	public static BadImplementationException developerSetViewIdOfIncompatibleViewForController(Field viewField) {
		String message = "View id does not belong to a CyborgView " + fieldDescription(viewField);
		return new BadImplementationException(message);
	}

	public static BadImplementationException couldNotFindViewForViewIdInLayout(Field viewField) {
		return new BadImplementationException("Could not find view for field" + fieldDescription(viewField));
	}

	public static BadImplementationException couldNotFindViewForViewIdInLayout(Class<? extends CyborgController> controllerType,
	                                                                           Class<? extends View> viewType,
	                                                                           @IdRes int viewId) {

		String viewIdAsName = getViewNameFromId(viewId);
		String message = "View of type '" + viewType.getSimpleName() + "' not found in '" + controllerType.getSimpleName() + "' for id: " + viewIdAsName;
		return new BadImplementationException(message);
	}

	public static BadImplementationException foundWrongViewType(Class<? extends CyborgController> controllerType,
	                                                            Class<? extends View> foundViewType,
	                                                            Class<? extends View> expectedViewType,
	                                                            @IdRes int viewId) {

		String message1 = "In controller: '" + controllerType.getSimpleName() + "' found WRONG view type for viewId: '" + getViewNameFromId(viewId) + "'\n";
		String message2 = "Expected view of type: '" + expectedViewType.getSimpleName() + "' but found with type: '" + foundViewType.getSimpleName() + "'";
		return new BadImplementationException(message1 + message2);
	}

	public static BadImplementationException wrongListenerToViewAssignment(Field viewField, View view, ViewListener listener) {
		String viewSimpleName = view.getClass().getSimpleName();
		String listenerSimpleName = listener.getMethodOwnerType().getSimpleName();
		return new BadImplementationException("Cannot assign '" + listener + "' listener to type: " + viewSimpleName + ", it does not inherit from super type: " + listenerSimpleName + fieldDescription(viewField));
	}

	public static BadImplementationException errorWhileAssigningListenerToView(Exception e) {
		return new BadImplementationException("Error while assigning listener to view", e);
	}

	public static ImplementationMissingException developerDidNotAddTheServiceToTheManifest(Class<? extends Service> serviceType) {
		String message = "<service android:name=\"" + serviceType.getName() + "\"/>";
		return new ImplementationMissingException("Service not present in your manifest. Add the following:\n\n    " + message);
	}

	public static ImplementationMissingException permissionMissingInManifest(String permission) {
		String message = "<uses-permission android:name=\"" + permission + "\"/>";
		return new ImplementationMissingException("Permission not present in your manifest. Add the following:\n\n    " + message);
	}

	public static ImplementationMissingException triedToCreateStackLayerWhenNotInStack() {
		return new ImplementationMissingException("Trying to create a stack layer while not in stack." +
			                                          "\nThis can also be due to a race condition while a transition between controllers within the stack!!" +
			                                          "\n    MAKE SURE you use the canExecute() method within the controller before adding another layer." +
			                                          "\n    alternatively you might want to change the BusyState duration to fit your stack transition!!");
	}

	public static <T> ImplementationMissingException missingAnnotationForRendererType(Class<? extends ItemRenderer<? extends T>> renderer) {
		String message = "In order to use this interface, you would need to annotate the " + renderer.getSimpleName() + " with " + ItemType.class.getSimpleName() + "";
		return new ImplementationMissingException(message);
	}

	public static BadImplementationException recyclerMismatchBetweenRendererTypesAndItemsTypesCounts(CyborgController controller,
	                                                                                                 Class<?>[] itemTypes,
	                                                                                                 Class<? extends ItemRenderer<?>>[] renderersTypes) {

		String message = "Recycler count conflict in" +
			"\nController: " + controller.getClass().getSimpleName() +
			"\n\nMismatch between:" +
			"\n  itemTypes(" + itemTypes.length + "): " + Arrays.toString(itemTypes) +
			"\nAND" +
			"\n  renderersTypes(" + renderersTypes.length + "): " + Arrays.toString(renderersTypes);
		return new BadImplementationException(message);
	}

	@NonNull
	private static String fieldDescription(Field viewField) {
		return "\n  Member: '" + viewField.getDeclaringClass()
		                                  .getSimpleName() + "." + viewField.getName() + "'\n  Class: '" + viewField.getDeclaringClass() + "'\n";
	}

	public static BadImplementationException userPassedTheControllerToBeSaved() {
		return new BadImplementationException("Do not pass the controller as the object to save it!! Cyborg is doing it perfectly on its own.");
	}

	public static BadImplementationException tryingToInjectViewWithoutModelDelgator() {
		return new BadImplementationException("modelDelegator == null");
	}

	public static BadImplementationException failedToInflateLayoutXml(Class<? extends CyborgController> controllerType, int layoutId, Throwable t) {
		String message = "Failed to inflate layout: '" + getLayoutNameFromId(layoutId) + "' in controller of type: '" + controllerType.getSimpleName() + "'";
		return new BadImplementationException(message, t);
	}

	public static BadImplementationException failedToInstantiateController(Class<? extends CyborgController> controllerType, Exception e) {
		String message = "Failed to instantiate controller of type: '" + controllerType.getSimpleName();
		return new BadImplementationException(message, e);
	}

	public static BadImplementationException initializingCyborgForTheSecondTime() {
		return new BadImplementationException("Trying to initialize Cyborg for the second time!");
	}

	public static BadImplementationException mustBeCalledOnMainThread() {
		return new BadImplementationException("---- MUST BE CALLED ON A UI THREAD ----  Method was called on the '" + Thread.currentThread().getName() + "'");
	}

	public static BadImplementationException tryingToOverrideExistingStackTransitionForKey(String key) {
		return new BadImplementationException("Transition with Key '" + key + "' already exists!!");
	}

	public static BadImplementationException cyborgWasNotInitializedProperly() {
		return new BadImplementationException("MUST first called from the onCreate of your custom application class!");
	}

	public static BadImplementationException cyborgWasInitializedFromTheWrongThread() {
		return new BadImplementationException("Must be called from UI thread to be more specific from the onCreate of your custom application class!");
	}

	public static BadImplementationException cyborgWasInitializedForTheSecondTime() {
		return new BadImplementationException("Seriously?? You've already created Cyborg, what is the point of calling this method from two places?? call it only from your custom application onCreate method!!!");
	}

	public static BadImplementationException stackLayerHasNoControllerType() {
		return new BadImplementationException("Stack Layer was not configured properly.. no controllerType");
	}

	public static ImplementationMissingException notificationMissingChannelId(Notification notification) {
		return new ImplementationMissingException("Notifications must be set with a channel ID. This is a requirement since Android O (api +26). " + notification.toString());
	}

	public static BadImplementationException notificationChannelDoesNotExist(String channelId, Notification notification) {
		return new BadImplementationException("Notification has been set with a NotificationChannelId{" + channelId + "}, but there is no NotificationChannel under that ID in the NotificationManager. Please add a NotificationChannel in the NotificationManager. This is a requirement since Android O (api +26). " + notification
			.toString());
	}

	public static ImplementationMissingException receiverWasFoundButIsDisabled(Class<? extends BroadcastReceiver> receiverType) {
		return new ImplementationMissingException("Broadcast Receiver of type: '" + receiverType.getName() + "' was found in the final manifest but it is disabled.. you need to enable it:\n" +
			                                          "      <receiver\n" +
			                                          "          android:name=\"" + receiverType.getName() + "\"\n" +
			                                          "          android:enabled=\"true\"\n" +
			                                          "          tools:replace=\"android:enabled\"\n" +
			                                          "      />");
	}

	public static ImplementationMissingException receiverWasNotInManifest(Class<? extends BroadcastReceiver> receiverType) {
		return new ImplementationMissingException("Broadcast Receiver of type: '" + receiverType.getName() + "' was NOT found in your final manifest.. you need to add it");
	}

	public static ImplementationMissingException missingPermissionsToPerformAction(String action, String permission, SecurityException e) {
		return new ImplementationMissingException("Cannot perform action: '" + action + "' Permission is missing in manifest, please add the following:\n" +
			                                          "    <uses-permission android:name=\"" + permission + "\"/>\n", e);
	}
}
