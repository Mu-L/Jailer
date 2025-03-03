/*
 * Copyright 2007 - 2025 Ralf Wisser.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.jailer.xml;

import org.w3c.dom.Document;

/**
 * Visits the nodes of a {@link Document}.
 * 
 * @author Ralf Wisser
 */
public interface NodeVisitor {

	/**
	 * Visits start of an element.
	 * 
	 * @param elementName the element name
	 * @param isRoot true if element is root
	 */
	void visitElementStart(String elementName, boolean isRoot, String[] attributeNames, String[] attributeValues);

	/**
	 * Visits end of an element.
	 * 
	 * @param elementName the element name
	 * @param isRoot true if element is root
	 */
	void visitElementEnd(String elementName, boolean isRoot);

	/**
	 * Visits text node. Pure whitespace text nodes will not be visited.
	 * 
	 * @param text the text
	 */
	void visitText(String text);

	/**
	 * Visits association element (namespace http://jailer.sf.net/association).
	 * 
	 * @param associationName the association name 
	 * @param name element/field name
	 */
	void visitAssociationElement(String associationName, String name);

	/**
	 * Visits comment.
	 * 
	 * @param comment the comment
	 */
	void visitComment(String comment);

}
