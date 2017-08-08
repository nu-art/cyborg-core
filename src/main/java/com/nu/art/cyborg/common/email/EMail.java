/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
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

package com.nu.art.cyborg.common.email;

import java.io.File;
import java.util.Vector;

public class EMail {

	private Vector<Recipient> to = new Vector<>();

	private Vector<Recipient> cc = new Vector<>();

	private Vector<Recipient> bcc = new Vector<>();

	private Vector<File> attachments = new Vector<>();

	private Recipient sender;

	private String subject;

	private String messageBody;

	public Vector<File> getAttachments() {
		return attachments;
	}

	public Recipient[] getBcc() {
		return bcc.toArray(new Recipient[bcc.size()]);
	}

	public Recipient[] getCc() {
		return cc.toArray(new Recipient[bcc.size()]);
	}

	public Recipient[] getTo() {
		return to.toArray(new Recipient[bcc.size()]);
	}

	public final void addRecipientTo(Recipient... recipients) {
		for (Recipient recipient : recipients)
			to.add(recipient);
	}

	public final void removeRecipientTo(Recipient... recipients) {
		for (Recipient recipient : recipients)
			to.remove(recipient);
	}

	public final void addRecipientCc(Recipient... recipients) {
		for (Recipient recipient : recipients)
			cc.add(recipient);
	}

	public final void removeRecipientCc(Recipient... recipients) {
		for (Recipient recipient : recipients)
			cc.remove(recipient);
	}

	public final void addRecipientBcc(Recipient... recipients) {
		for (Recipient recipient : recipients)
			bcc.add(recipient);
	}

	public final void removeRecipientBcc(Recipient... recipients) {
		for (Recipient recipient : recipients)
			bcc.remove(recipient);
	}

	public void addAttachments(File... attachments) {
		for (File attachment : attachments)
			this.attachments.add(attachment);
	}

	public void removeAttachment(File... attachments) {
		for (File attachment : attachments)
			this.attachments.remove(attachment);
	}

	public Recipient getSender() {
		return sender;
	}

	public void setSender(Recipient sender) {
		this.sender = sender;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}
}
