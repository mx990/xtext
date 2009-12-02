/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.resource.impl;

import java.util.Collection;

import org.eclipse.xtext.junit.AbstractXtextTests;
import org.eclipse.xtext.linking.LangATestLanguageStandaloneSetup;
import org.eclipse.xtext.linking.langATestLanguage.LangATestLanguagePackage;
import org.eclipse.xtext.linking.langATestLanguage.Main;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.XtextResource;

import com.google.common.collect.Collections2;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class DefaultReferenceDescriptionTest extends AbstractXtextTests {
	
	public void testgetReferenceDescriptions() throws Exception {
		with(new LangATestLanguageStandaloneSetup());
		XtextResource resource = getResource("type A extends B type B", "foo.langatestlanguage");
		IResourceDescription resDesc = resource.getResourceServiceProvider().getResourceDescriptionManager().getResourceDescription(resource);
		Iterable<IReferenceDescription> descriptions = resDesc.getReferenceDescriptions();
		Collection<IReferenceDescription> collection = Collections2.forIterable(descriptions);
		assertEquals(1,collection.size());
		IReferenceDescription refDesc = descriptions.iterator().next();
		Main m = (Main) resource.getParseResult().getRootASTElement();
		assertEquals(m.getTypes().get(0),resource.getResourceSet().getEObject(refDesc.getSourceEObjectUri(),false));
		assertEquals(m.getTypes().get(1),resource.getResourceSet().getEObject(refDesc.getTargetEObjectUri(),false));
		assertEquals(-1,refDesc.getIndexInList());
		assertEquals(LangATestLanguagePackage.Literals.TYPE__EXTENDS,refDesc.getEReference());
	}
	
	
}
