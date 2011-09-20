/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.typing;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static java.util.Collections.*;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmAnyTypeReference;
import org.eclipse.xtext.common.types.JvmArrayType;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmDelegateTypeReference;
import org.eclipse.xtext.common.types.JvmExecutable;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericArrayTypeReference;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmMultiTypeReference;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmSynonymTypeReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeConstraint;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeParameterDeclarator;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmUpperBound;
import org.eclipse.xtext.common.types.JvmVoid;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.util.SuperTypeCollector;
import org.eclipse.xtext.common.types.util.TypeArgumentContext;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XAbstractWhileExpression;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XBooleanLiteral;
import org.eclipse.xtext.xbase.XCasePart;
import org.eclipse.xtext.xbase.XCastedExpression;
import org.eclipse.xtext.xbase.XCatchClause;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XInstanceOfExpression;
import org.eclipse.xtext.xbase.XIntLiteral;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XNullLiteral;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.XThrowExpression;
import org.eclipse.xtext.xbase.XTryCatchFinallyExpression;
import org.eclipse.xtext.xbase.XTypeLiteral;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.impl.FeatureCallToJavaMapping;
import org.eclipse.xtext.xbase.jvmmodel.ILogicalContainerProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Sven Efftinge
 * @author Sebastian Zarnekow
 */
@Singleton
public class XbaseTypeProvider extends AbstractTypeProvider {

	@Inject
	private TypesFactory factory;
	
	@Inject
	private FeatureCallToJavaMapping featureCall2javaMapping;

	@Inject
	private Closures closures;

	@Inject
	private SuperTypeCollector collector;
	
	@Inject
	private ILogicalContainerProvider expressionContext;
	
	@Override
	protected JvmTypeReference _expectedType(EObject obj, EReference reference, int index, boolean rawType) {
		Object ele = obj.eGet(reference);
		if (ele instanceof List) {
			ele = ((List<?>)ele).get(index);
		}
		if (ele instanceof XExpression) {
			JvmIdentifiableElement element = expressionContext.getLogicalContainer((XExpression) ele);
			if (element instanceof JvmOperation) {
				return ((JvmOperation) element).getReturnType();
			}
			if (element instanceof JvmField) {
				return ((JvmField) element).getType();
			}
		}
		return null;
	}
	
	@Override
	protected JvmTypeReference typeForIdentifiableDispatcherInvoke(JvmIdentifiableElement identifiable, boolean rawType) {
		if (identifiable instanceof JvmConstructor) {
			return _typeForIdentifiable((JvmConstructor)identifiable, rawType);
		} else if (identifiable instanceof JvmField) {
			return _typeForIdentifiable((JvmField)identifiable, rawType);
		} else if (identifiable instanceof JvmFormalParameter) {
			return _typeForIdentifiable((JvmFormalParameter)identifiable, rawType);
		} else if (identifiable instanceof JvmOperation) {
			return _typeForIdentifiable((JvmOperation)identifiable, rawType);
		} else if (identifiable instanceof JvmType) {
			return _typeForIdentifiable((JvmType)identifiable, rawType);
		} else if (identifiable instanceof XCasePart) {
			return _typeForIdentifiable((XCasePart)identifiable, rawType);
		} else if (identifiable instanceof XSwitchExpression) {
			return _typeForIdentifiable((XSwitchExpression)identifiable, rawType);
		} else if (identifiable instanceof XVariableDeclaration) {
			return _typeForIdentifiable((XVariableDeclaration)identifiable, rawType);
		} else {
			return super.typeForIdentifiableDispatcherInvoke(identifiable, rawType);
		}
	}
	
	@Override
	protected JvmTypeReference typeDispatcherInvoke(XExpression expression, boolean rawType) {
		if (expression instanceof XAbstractFeatureCall) {
			return _type((XAbstractFeatureCall)expression, rawType);
		} else if (expression instanceof XAbstractWhileExpression) {
			return _type((XAbstractWhileExpression)expression, rawType);
		} else if (expression instanceof XBlockExpression) {
			return _type((XBlockExpression)expression, rawType);
		} else if (expression instanceof XBooleanLiteral) {
			return _type((XBooleanLiteral)expression, rawType);
		} else if (expression instanceof XCastedExpression) {
			return _type((XCastedExpression)expression, rawType);
		} else if (expression instanceof XClosure) {
			return _type((XClosure)expression, rawType);
		} else if (expression instanceof XConstructorCall) {
			return _type((XConstructorCall)expression, rawType);
		} else if (expression instanceof XForLoopExpression) {
			return _type((XForLoopExpression)expression, rawType);
		} else if (expression instanceof XIfExpression) {
			return _type((XIfExpression)expression, rawType);
		} else if (expression instanceof XInstanceOfExpression) {
			return _type((XInstanceOfExpression)expression, rawType);
		} else if (expression instanceof XIntLiteral) {
			return _type((XIntLiteral)expression, rawType);
		} else if (expression instanceof XNullLiteral) {
			return _type((XNullLiteral)expression, rawType);
		} else if (expression instanceof XReturnExpression) {
			return _type((XReturnExpression)expression, rawType);
		} else if (expression instanceof XStringLiteral) {
			return _type((XStringLiteral)expression, rawType);
		} else if (expression instanceof XSwitchExpression) {
			return _type((XSwitchExpression)expression, rawType);
		} else if (expression instanceof XThrowExpression) {
			return _type((XThrowExpression)expression, rawType);
		} else if (expression instanceof XTryCatchFinallyExpression) {
			return _type((XTryCatchFinallyExpression)expression, rawType);
		} else if (expression instanceof XTypeLiteral) {
			return _type((XTypeLiteral)expression, rawType);
		} else if (expression instanceof XVariableDeclaration) {
			return _type((XVariableDeclaration)expression, rawType);
		} else { 
			return super.typeDispatcherInvoke(expression, rawType);
		}
	}
	
	@Override
	protected JvmTypeReference expectedTypeDispatcherInvoke(EObject container, EReference reference, int index,
			boolean rawType) {
		if (container instanceof XAssignment) {
			return _expectedType((XAssignment)container, reference, index, rawType);
		} else if (container instanceof XBinaryOperation) {
			return _expectedType((XBinaryOperation)container, reference, index, rawType);
		} else if (container instanceof XAbstractFeatureCall) {
			return _expectedType((XAbstractFeatureCall)container, reference, index, rawType);
		} else if (container instanceof XAbstractWhileExpression) {
			return _expectedType((XAbstractWhileExpression)container, reference, index, rawType);
		} else if (container instanceof XBlockExpression) {
			return _expectedType((XBlockExpression)container, reference, index, rawType);
		} else if (container instanceof XCasePart) {
			return _expectedType((XCasePart)container, reference, index, rawType);
		} else if (container instanceof XCastedExpression) {
			return _expectedType((XCastedExpression)container, reference, index, rawType);
		} else if (container instanceof XCatchClause) {
			return _expectedType((XCatchClause)container, reference, index, rawType);
		} else if (container instanceof XClosure) {
			return _expectedType((XClosure)container, reference, index, rawType);
		} else if (container instanceof XConstructorCall) {
			return _expectedType((XConstructorCall)container, reference, index, rawType);
		} else if (container instanceof XForLoopExpression) {
			return _expectedType((XForLoopExpression)container, reference, index, rawType);
		} else if (container instanceof XIfExpression) {
			return _expectedType((XIfExpression)container, reference, index, rawType);
		} else if (container instanceof XReturnExpression) {
			return _expectedType((XReturnExpression)container, reference, index, rawType);
		} else if (container instanceof XSwitchExpression) {
			return _expectedType((XSwitchExpression)container, reference, index, rawType);
		} else if (container instanceof XThrowExpression) {
			return _expectedType((XThrowExpression)container, reference, index, rawType);
		} else if (container instanceof XTryCatchFinallyExpression) {
			return _expectedType((XTryCatchFinallyExpression)container, reference, index, rawType);
		} else if (container instanceof XVariableDeclaration) {
			return _expectedType((XVariableDeclaration)container, reference, index, rawType);
		} else {
			return super.expectedTypeDispatcherInvoke(container, reference, index, rawType);
		}
	}
	
	protected JvmTypeReference _expectedType(XAssignment assignment, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XASSIGNMENT__VALUE) {
			JvmIdentifiableElement feature = assignment.getFeature();
			JvmTypeReference receiverType = getReceiverType(assignment, rawType);
			if (feature instanceof JvmOperation) {
				JvmOperation operation = (JvmOperation) feature;
				XExpression expression = getExpression(assignment, reference, index);
				List<XExpression> actualArguments = featureCall2javaMapping.getActualArguments(assignment);
				int actualIndex = actualArguments.indexOf(expression);
				JvmFormalParameter parameter = getParam(operation, actualIndex);
				if (parameter != null) {
					JvmTypeReference declaredType = parameter.getParameterType();
					TypeArgumentContext context = getFeatureCallTypeArgContext(assignment, reference, index, rawType);
					return context.getLowerBound(declaredType);
				}
				return null;
			} else {
				final JvmTypeReference type = getTypeForIdentifiable(feature, rawType);
				if (rawType)
					return type;
				TypeArgumentContext context = getTypeArgumentContextProvider().getReceiverContext(receiverType);
				return context.getLowerBound(type);
			}
		}
		return null;
	}

	protected XExpression getExpression(EObject object, EReference reference, int index) {
		if (index == -1) {
			return (XExpression) object.eGet(reference, true);
		} else {
			List<?> expressions = (List<?>) object.eGet(reference, true);
			XExpression result = (XExpression) expressions.get(index);
			return result;
		}
	}

	protected JvmTypeReference _expectedType(XAbstractFeatureCall featureCall, EReference reference, int index,
			boolean rawType) {
		if (featureCall.getFeature() == null || featureCall.getFeature().eIsProxy())
			return null;
		if ((featureCall instanceof XMemberFeatureCall && (reference == XbasePackage.Literals.XMEMBER_FEATURE_CALL__MEMBER_CALL_ARGUMENTS || reference == XbasePackage.Literals.XMEMBER_FEATURE_CALL__MEMBER_CALL_TARGET))
				|| (featureCall instanceof XFeatureCall && (reference == XbasePackage.Literals.XFEATURE_CALL__FEATURE_CALL_ARGUMENTS || reference == XbasePackage.Literals.XABSTRACT_FEATURE_CALL__IMPLICIT_RECEIVER))) {
			if (featureCall.getFeature() instanceof JvmOperation) {
				JvmOperation operation = (JvmOperation) featureCall.getFeature();
				XExpression argumentExpression = getExpression(featureCall, reference, index);
				List<XExpression> actualArguments = featureCall2javaMapping.getActualArguments(featureCall);
				TypeArgumentContext context = getFeatureCallTypeArgContext(featureCall, reference, index, rawType);
				int argumentIndex = actualArguments.indexOf(argumentExpression);
				if (argumentIndex >= 0) {
					if (operation.isVarArgs()) {
						if (argumentIndex >= operation.getParameters().size() - 1) {
							JvmTypeReference result = getExpectedVarArgType(operation, context, rawType);
							return result;
						}
					}
					if (argumentIndex >= operation.getParameters().size())
						return null;
					JvmFormalParameter parameter = getParam(operation, argumentIndex);
					final JvmTypeReference parameterType = parameter.getParameterType();
					final JvmTypeReference result = context.getLowerBound(parameterType);
					return result;
				} else {
					final JvmTypeReference declaringType = getTypeReferences().createTypeRef(operation.getDeclaringType());
					if (rawType)
						return declaringType;
					if (!rawType && argumentExpression != null) {
						JvmTypeReference receiverType = getType(argumentExpression, rawType);
						if (receiverType != null) {
							JvmTypeReference result = context.getLowerBound(receiverType);
							if (result != null && result.getType() instanceof JvmTypeParameter)
								return result;
						}
					}
					JvmTypeReference result = context.getLowerBound(declaringType);
					return result;
				}
			} else if (featureCall.getFeature() instanceof JvmField) {
				JvmField field = (JvmField) featureCall.getFeature();
				// TODO: lower bound for fields? resolve type parameters?
				return getTypeReferences().createTypeRef(field.getDeclaringType());
			}
		}
		return null;
	}

	protected JvmTypeReference _expectedType(XClosure closure, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XCLOSURE__EXPRESSION) {
			JvmTypeReference functionType = getExpectedType(closure, rawType);
			if (functionType != null) {
				JvmOperation operation = closures.findImplementingOperation(functionType, closure.eResource());
				JvmTypeReference declaredReturnType = operation.getReturnType();
//				if (result==null || result.getType() instanceof JvmTypeParameter)
//					return null;
				TypeArgumentContext receiverContext = getTypeArgumentContextProvider().getReceiverContext(functionType);
				JvmTypeReference result = receiverContext.getUpperBound(declaredReturnType, closure);
				return result;
			}
		}
		return null;
	}

	@Override
	protected JvmTypeReference handleCycleGetExpectedType(XExpression expression, boolean rawType) {
		Triple<EObject, EReference, Integer> info = getContainingInfo(expression);
		if (info.getFirst() instanceof XAbstractFeatureCall) {
			JvmTypeReference result = _expectedType((XAbstractFeatureCall) info.getFirst(), info.getSecond(),
					info.getThird(), rawType);
			return result;
		}
		return super.handleCycleGetExpectedType(expression, rawType);
	}

	protected TypeArgumentContext getFeatureCallTypeArgContext(
			XAbstractFeatureCall expr, 
			EReference reference,
			int index, 
			boolean rawType) {
		JvmTypeReference receiverType = getReceiverType(expr, rawType);
		if (expr.getFeature() instanceof JvmOperation) {
			JvmOperation operation = (JvmOperation) expr.getFeature();
			if (expr.getTypeArguments().isEmpty()) {
//				receiverType = convertIfNeccessary(expr, receiverType, operation, rawType);
				JvmTypeReference expectedType = getExpectedType(expr, rawType);
				JvmTypeReference[] argTypes = getArgumentTypes(expr, rawType);
				TypeArgumentContext context = getTypeArgumentContextProvider().getInferredMethodInvocationContext(operation,
						receiverType, expectedType, argTypes);
				return context;
			} else {
				TypeArgumentContext result = getTypeArgumentContextProvider().getExplicitMethodInvocationContext(operation,
						receiverType, expr.getTypeArguments());
				return result;
			}
		} else {
			return getTypeArgumentContextProvider().getReceiverContext(receiverType);
		}
	}

	protected JvmTypeReference[] getArgumentTypes(XAbstractFeatureCall expr, boolean rawType) {
		List<XExpression> arguments = featureCall2javaMapping.getActualArguments(expr);
		JvmExecutable executable = (JvmExecutable) expr.getFeature();
		return getArgumentTypes(executable, arguments, rawType);
	}

	protected JvmTypeReference[] getArgumentTypes(JvmExecutable executable, List<XExpression> actualArguments,
			boolean rawType) {
		JvmTypeReference[] argTypes = new JvmTypeReference[actualArguments.size()];
		if (actualArguments.isEmpty())
			return argTypes;
		for (int i = 0; i < argTypes.length; i++) {
			XExpression arg = actualArguments.get(i);
			JvmFormalParameter parameter = getParam(executable, i);
			if (parameter != null) {
				final JvmTypeReference type = getType(arg, rawType);
				if (type != null) {
//					JvmTypeReference parameterType = getTypeForIdentifiable(parameter, rawType);
//					if (parameterType != null) {
////						JvmTypeReference synonymType = this.synonymTypesProvider.findCompatibleSynonymType(type, parameterType.getType());
//						argTypes[i] = parameterType; //synonymType != null ? synonymType : type;
//					} else {
						argTypes[i] = type;
//					}
				} else {
					JvmTypeReference parameterType = getTypeForIdentifiable(parameter, rawType);
					argTypes[i] = parameterType;
				}
			}
		}
		return argTypes;
	}

	protected JvmFormalParameter getParam(JvmExecutable executable, int i) {
		if (executable.getParameters().size() <= i) {
			if (executable.isVarArgs())
				return executable.getParameters().get(executable.getParameters().size() - 1);
			return null;
		}
		return executable.getParameters().get(i);
	}

	protected JvmTypeReference getReceiverType(XAbstractFeatureCall expr, boolean rawType) {
		XExpression receiver = featureCall2javaMapping.getActualReceiver(expr);
		JvmTypeReference receiverType = null;
		if (receiver != null)
			receiverType = getType(receiver, rawType);
		if (receiverType != null) {
			Set<JvmTypeReference> synonymTypes = synonymTypesProvider.getSynonymTypes(receiverType, false);
			if (!synonymTypes.isEmpty()) {
				JvmSynonymTypeReference result = factory.createJvmSynonymTypeReference();
				if (receiverType.eResource() != null) {
					JvmDelegateTypeReference delegate = factory.createJvmDelegateTypeReference();
					delegate.setDelegate(receiverType);
					result.getReferences().add(delegate);
				} else {
					result.getReferences().add(receiverType);
				}
				for(JvmTypeReference synonym: synonymTypes) {
					if (synonym.eResource() != null) {
						JvmDelegateTypeReference delegate = factory.createJvmDelegateTypeReference();
						delegate.setDelegate(synonym);
						result.getReferences().add(delegate);
					} else {
						result.getReferences().add(synonym);
					}
				}
				return result;
			}
		}
		return receiverType;
	}

	protected JvmTypeReference _expectedType(XBinaryOperation expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XBINARY_OPERATION__RIGHT_OPERAND
				&& expr.getFeature() instanceof JvmOperation) {
			JvmOperation feature = (JvmOperation) expr.getFeature();
			JvmFormalParameter parameter = getLast(feature.getParameters());
			TypeArgumentContext context = getFeatureCallTypeArgContext(expr, reference, index, rawType);
			final JvmTypeReference parameterType = parameter.getParameterType();
			JvmTypeReference result = context.getLowerBound(parameterType);
			return result;
		}
		if (reference == XbasePackage.Literals.XBINARY_OPERATION__LEFT_OPERAND
				&& expr.getFeature() instanceof JvmOperation) {
			JvmOperation operation = (JvmOperation) expr.getFeature();
			if (operation.getParameters().size() > 1) {
				JvmFormalParameter parameter = operation.getParameters().get(0);
				TypeArgumentContext context = getFeatureCallTypeArgContext(expr, reference, index, rawType);
				final JvmTypeReference parameterType = parameter.getParameterType();
				JvmTypeReference resolved = context.resolve(parameterType);
				return resolved;
			} else if (!operation.isStatic()) {
				// expectation for member call target for operations on objects 
				XExpression argumentExpression = getExpression(expr, reference, index);
				TypeArgumentContext context = getFeatureCallTypeArgContext(expr, reference, index, rawType);
				final JvmTypeReference declaringType = getTypeReferences().createTypeRef(operation.getDeclaringType());
				if (rawType)
					return declaringType;
				if (!rawType && argumentExpression != null) {
					JvmTypeReference receiverType = getType(argumentExpression, rawType);
					if (receiverType != null) {
						JvmTypeReference result = context.getLowerBound(receiverType);
						if (result != null && result.getType() instanceof JvmTypeParameter)
							return result;
					}
				}
				JvmTypeReference result = context.getLowerBound(declaringType);
				return result;
			}
		}
		return null;
	}

	protected JvmTypeReference _expectedType(XVariableDeclaration expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XVARIABLE_DECLARATION__RIGHT) {
			final JvmTypeReference type = expr.getType();
			return type;
		}
		return null; // no expectations
	}

	protected JvmTypeReference _expectedType(XConstructorCall expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XCONSTRUCTOR_CALL__ARGUMENTS) {
			JvmExecutable feature = expr.getConstructor();
			TypeArgumentContext typeArgumentContext = getTypeArgumentContextProvider().getReceiverContext(null);
			if (feature instanceof JvmConstructor && !feature.eIsProxy()) {
				List<JvmTypeReference> argumentTypes = Lists.newArrayListWithCapacity(expr.getArguments().size());
				if (expr.getTypeArguments().isEmpty()) {
					for(XExpression argument: expr.getArguments()) {
						JvmTypeReference argumentType = getType(argument, true);
						argumentTypes.add(argumentType);
					}
					typeArgumentContext = getTypeArgumentContextProvider().injectArgumentTypeContext(
							typeArgumentContext, 
							(JvmConstructor)feature, 
							null,
							true,
							argumentTypes.toArray(new JvmTypeReference[argumentTypes.size()])
					);
				} else {
					typeArgumentContext = getTypeArgumentContextProvider().getExplicitMethodInvocationContext(
							(JvmTypeParameterDeclarator) feature.getDeclaringType(), null, expr.getTypeArguments());
				}
			}
			if (index >= feature.getParameters().size()) {
				if (feature.isVarArgs()) {
					return getExpectedVarArgType(feature, typeArgumentContext, rawType);
				}
				return null;
			}
			if (feature.isVarArgs() && index == feature.getParameters().size() - 1) {
				return getExpectedVarArgType(feature, typeArgumentContext, rawType);
			}
			JvmFormalParameter parameter = feature.getParameters().get(index);
			JvmTypeReference parameterType = parameter.getParameterType();
			return typeArgumentContext.getLowerBound(parameterType);
		}
		return null;
//		return getExpectedType(expr, rawType);
	}

	protected JvmTypeReference getExpectedVarArgType(JvmExecutable feature, TypeArgumentContext typeArgumentContext,
			boolean rawType) {
		JvmFormalParameter lastParameter = feature.getParameters().get(feature.getParameters().size() - 1);
		JvmTypeReference parameterType = lastParameter.getParameterType();
		if (parameterType instanceof JvmGenericArrayTypeReference) {
			JvmTypeReference componentType = ((JvmGenericArrayTypeReference) parameterType).getComponentType();
			return typeArgumentContext.getLowerBound(componentType);
		} else {
			throw new IllegalStateException("Var arg parameter has to be an array type");
		}
	}

	protected JvmTypeReference _expectedType(XBlockExpression expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XBLOCK_EXPRESSION__EXPRESSIONS) {
			// if last expression
			if (index + 1 == expr.getExpressions().size()) {
				return getExpectedType(expr, rawType);
			} else {
				return null; // no expectation
			}
		}
		throw new IllegalStateException("Unhandled reference " + reference);
	}

	protected JvmTypeReference _expectedType(XIfExpression expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XIF_EXPRESSION__IF) {
			return getTypeReferences().getTypeForName(Boolean.TYPE, expr);
		}
		return getExpectedType(expr, rawType);
	}

	protected JvmTypeReference _expectedType(XForLoopExpression expr, EReference reference, int index, boolean rawType) {
		// Unless we can have multiple possible expected types (i.e. array and iterable), we shouldn't expect anything here
		// The conformance test is done explicitly in the validator.
		return null; // no expectations
	}

	protected JvmTypeReference _expectedType(XAbstractWhileExpression expr, EReference reference, int index,
			boolean rawType) {
		if (reference == XbasePackage.Literals.XABSTRACT_WHILE_EXPRESSION__PREDICATE) {
			final JvmTypeReference typeForName = getTypeReferences().getTypeForName(Boolean.TYPE, expr);
			return typeForName;
		}
		return null; // no other expectations
	}

	protected JvmTypeReference _expectedType(XTryCatchFinallyExpression expr, EReference reference, int index,
			boolean rawType) {
		if (reference == XbasePackage.Literals.XTRY_CATCH_FINALLY_EXPRESSION__EXPRESSION) {
			return getExpectedType(expr, rawType);
		}
		if (reference == XbasePackage.Literals.XTRY_CATCH_FINALLY_EXPRESSION__CATCH_CLAUSES) {
			return getExpectedType(expr, rawType);
		}
		return null; // no other expectations
	}

	protected JvmTypeReference _expectedType(XCatchClause expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XCATCH_CLAUSE__DECLARED_PARAM) {
			return getTypeReferences().getTypeForName(Throwable.class, expr);
		}
		return getExpectedType((XExpression) expr.eContainer(), rawType);
	}

	protected JvmTypeReference _expectedType(XCastedExpression expr, EReference reference, int index, boolean rawType) {
		// SE: This was previously explicitly set to null :
		// "return null; // no expectations!"
		// Unfortunately there was no comment explaining why this was the case also no test besides the one which explicitly tested this was failing so I changed it back.
		return expr.getType(); 
	}

	protected JvmTypeReference _expectedType(XThrowExpression expr, EReference reference, int index, boolean rawType) {
		return getTypeReferences().getTypeForName(Throwable.class, expr);
	}

	protected JvmTypeReference _expectedType(XReturnExpression expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XRETURN_EXPRESSION__EXPRESSION) {
			XClosure closure = EcoreUtil2.getContainerOfType(expr, XClosure.class);
			if (closure!=null) {
				JvmTypeReference expectedReturnType = getExpectedType(closure.getExpression());
				if (expectedReturnType != null) {
					return expectedReturnType;
				}
			}
			return getTypeReferences().getTypeForName(Object.class, expr);
		}
		return null; // no expectations!
	}

	protected JvmTypeReference _expectedType(XSwitchExpression expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XSWITCH_EXPRESSION__SWITCH) {
			return null; // no expectations
		}
		return getExpectedType(expr, rawType);
	}

	protected JvmTypeReference _expectedType(XCasePart expr, EReference reference, int index, boolean rawType) {
		if (reference == XbasePackage.Literals.XCASE_PART__TYPE_GUARD) {
			return getTypeReferences().getTypeForName(Class.class, expr);
		}
		if (reference == XbasePackage.Literals.XCASE_PART__CASE) {
			final XSwitchExpression switchExpr = (XSwitchExpression) expr.eContainer();
			if (switchExpr.getSwitch() == null) {
				return getTypeReferences().getTypeForName(Boolean.TYPE, expr);
			}
			return null;
		}
		if (reference == XbasePackage.Literals.XCASE_PART__THEN) {
			return getExpectedType((XSwitchExpression) expr.eContainer(), rawType);
		}
		return null;
	}

	protected JvmTypeReference _type(XIfExpression object, boolean rawType) {
		List<JvmTypeReference> returnTypes = newArrayList();
		final JvmTypeReference thenType = getType(object.getThen(), rawType);
		if (thenType != null)
			returnTypes.add(thenType);
		JvmTypeReference elseType = getTypeReferences().createAnyTypeReference(object);
		if (object.getElse()!=null) {
			elseType = getType(object.getElse(), rawType);
		}
		if (elseType != null)
			returnTypes.add(elseType);
		return getCommonType(returnTypes);
	}

	/**
	 * Returns the common type of the given types. 
	 * {@link #isFilteredFromCommonTypesList(JvmTypeReference) Filters} for primitive voids
	 * and unresolved types prior to asking the 
	 * {@link org.eclipse.xtext.common.types.util.TypeConformanceComputer TypeConformanceComputer}.
	 * 
	 */
	protected JvmTypeReference getCommonType(List<JvmTypeReference> types) {
		if (types.isEmpty()) {
			return null;
		}
		if (types.size() == 1) {
			JvmTypeReference result = getTypeConformanceComputer().getCommonSuperType(types);
			return result;
		}
		List<JvmTypeReference> filteredTypes = Lists.newArrayListWithExpectedSize(types.size());
		for(JvmTypeReference reference: types) {
			if (!isFilteredFromCommonTypesList(reference)) {
				filteredTypes.add(reference);
			}
		}
		if (filteredTypes.isEmpty()) {
			JvmTypeReference result = getTypeConformanceComputer().getCommonSuperType(types);
			return result;
		}
		JvmTypeReference result = getTypeConformanceComputer().getCommonSuperType(filteredTypes);
		return result;
	}

	protected boolean isFilteredFromCommonTypesList(JvmTypeReference reference) {
		if (reference == null)
			return true;
		if (reference instanceof JvmMultiTypeReference && ((JvmMultiTypeReference) reference).getReferences().isEmpty())
			return true;
		if (reference instanceof JvmAnyTypeReference)
			return false;
		// TODO use IEarlyExitComputer
		if (reference.getType() instanceof JvmVoid && !reference.getType().eIsProxy())
			return true;
		return false;
	}

	protected JvmTypeReference _type(XSwitchExpression object, boolean rawType) {
		List<JvmTypeReference> returnTypes = Lists.newArrayList();
		EList<XCasePart> cases = object.getCases();
		for (XCasePart xCasePart : cases) {
			final JvmTypeReference unconverted = getType(xCasePart.getThen(), rawType);
			if (unconverted != null)
				returnTypes.add(unconverted);
		}
		if (object.getDefault() != null) {
			final JvmTypeReference unconverted = getType(object.getDefault(), rawType);
			if (unconverted != null)
				returnTypes.add(unconverted);
		}
		return getCommonType(returnTypes);
	}

	protected JvmTypeReference _type(XBlockExpression object, boolean rawType) {
		List<XExpression> expressions = object.getExpressions();
		if (expressions.isEmpty())
			return getTypeReferences().createAnyTypeReference(object);
		final JvmTypeReference result = getType(expressions.get(expressions.size() - 1), rawType);
		return result;
	}

	protected JvmTypeReference _type(XVariableDeclaration object, boolean rawType) {
		return getPrimitiveVoid(object);
	}

	protected JvmTypeReference _type(XConstructorCall constructorCall, boolean rawType) {
		JvmConstructor constructor = constructorCall.getConstructor();
		if (constructor == null || constructor.eIsProxy())
			return null;
		JvmTypeReference constructorResultType = getTypeForIdentifiable(constructor, rawType);
		JvmTypeParameterDeclarator nearestTypeParameterDeclarator = getNearestTypeParameterDeclarator(constructorCall);
		if (isResolved(constructorResultType, nearestTypeParameterDeclarator, rawType)) {
			return constructorResultType;
		}
		rawType = false;
		JvmTypeParameterDeclarator typeParameterDeclarator = (JvmTypeParameterDeclarator) constructorResultType.getType();
		if (constructorCall.getTypeArguments().isEmpty() && (!constructor.getTypeParameters().isEmpty() || !typeParameterDeclarator.getTypeParameters().isEmpty())) {
			TypeArgumentContext context = getTypeArgumentContextProvider().getNullContext();
			JvmTypeReference result = constructorResultType;
			JvmTypeReference[] argumentTypes = getArgumentTypes(constructor, constructorCall.getArguments(), rawType);
			if (argumentTypes.length != 0 || constructor.isVarArgs()) {
				context = getTypeArgumentContextProvider().injectArgumentTypeContext(context, constructor, constructorResultType, true, argumentTypes);
				result = context.getUpperBound(constructorResultType, constructorCall);
				if (isResolved(result, nearestTypeParameterDeclarator, rawType)) {
					return result;
				}
			}
			JvmTypeReference expectedType = getExpectedType(constructorCall, rawType);
			if (expectedType != null) {
				context = getTypeArgumentContextProvider().injectExpectedTypeContext(context, constructor, constructorResultType, expectedType);
				result = context.getUpperBound(constructorResultType, constructorCall);
				if (isResolved(result, nearestTypeParameterDeclarator, rawType)) {
					return result;
				}
			}
			// try again to resolve the type parameters, this time with empty var args
			if (constructor.isVarArgs() && constructor.getParameters().size() > argumentTypes.length) {
				context = getTypeArgumentContextProvider().injectArgumentTypeContext(context, constructor, constructorResultType, false, argumentTypes);
				result = context.getUpperBound(constructorResultType, constructorCall);
				if (isResolved(result, nearestTypeParameterDeclarator, rawType)) {
					return result;
				}
			}
			if (!isResolved(result, nearestTypeParameterDeclarator, rawType)) {
				if (result instanceof JvmTypeParameter) {
					JvmTypeParameter type = (JvmTypeParameter) result.getType();
					JvmTypeReference upperBound = null;
					for (JvmTypeConstraint constraint : type.getConstraints()) {
						if (constraint instanceof JvmUpperBound) {
							if (upperBound != null) {
								return getType(constructorCall, false);
							}
							JvmTypeReference reference = constraint.getTypeReference();
							upperBound = context.getUpperBound(reference, constructorCall);
						}
					}
					if (upperBound != null && !(upperBound.getType() instanceof JvmTypeParameter))
						return upperBound;
					return getType(constructorCall, false);
				}
			}
			result = context.getUpperBound(constructorResultType, constructor);
			return result;
		} else {
			TypeArgumentContext context = getTypeArgumentContextProvider().getExplicitMethodInvocationContext(
					typeParameterDeclarator,
					null, constructorCall.getTypeArguments());
			return context.getUpperBound(constructorResultType, constructor);
		}
	}
	
	protected JvmTypeReference _type(XBooleanLiteral object, boolean rawType) {
		return getTypeReferences().getTypeForName(Boolean.TYPE, object);
	}

	protected JvmTypeReference _type(XNullLiteral object, boolean rawType) {
		JvmAnyTypeReference result = getTypeReferences().createAnyTypeReference(object);
		return result;
	}

	protected JvmTypeReference _type(XIntLiteral object, boolean rawType) {
		return getTypeReferences().getTypeForName(Integer.TYPE, object);
	}

	protected JvmTypeReference _type(XStringLiteral object, boolean rawType) {
		return getTypeReferences().getTypeForName(String.class, object);
//		if (object.getValue().length() != 1)
//			return getTypeReferences().getTypeForName(String.class, object);
//		JvmTypeReference stringType = getTypeReferences().getTypeForName(String.class, object);
//		JvmTypeReference charType = getTypeReferences().getTypeForName(Character.TYPE, object);
//		XSynonymTypeReference result = xtypesFactory.createXSynonymTypeReference();
//		result.getSynonymes().add(stringType);
//		result.getSynonymes().add(charType);
//		return result;
	}

	protected JvmTypeReference _type(XClosure object, boolean rawType) {
		if (rawType) {
			JvmParameterizedTypeReference result = closures.createRawFunctionTypeRef(
					object, object.getFormalParameters().size());
			return result;
		}
		JvmTypeReference expectedType = getExpectedType(object, rawType);
		JvmOperation singleMethod = null;
		JvmTypeReference returnType = null;
		if (expectedType != null) {
			singleMethod = closures.findImplementingOperation(expectedType, object.eResource());
		}
		returnType = getCommonReturnType(object.getExpression(), true);
		if (!rawType && returnType instanceof JvmAnyTypeReference) {
			JvmTypeReference type = getExpectedType(object.getExpression());
			if (singleMethod != null) {
				if (isResolved(type, singleMethod, rawType))
					returnType = type;
			} else {
				returnType = type;
			}
		}
		List<JvmTypeReference> parameterTypes = Lists.newArrayList();
		List<JvmFormalParameter> params = object.getFormalParameters();
		for (JvmFormalParameter param : params) {
			parameterTypes.add(param.getParameterType());
		}
		// inferred argument types?
		if (!params.isEmpty() && singleMethod != null) {
			TypeArgumentContext context = getTypeArgumentContextProvider().getReceiverContext(expectedType);
			for (int i = 0; i < params.size(); i++) {
				JvmTypeReference resultParam = parameterTypes.get(i);
				if (resultParam == null) {
					JvmFormalParameter p = getParam(singleMethod, i);
					final JvmTypeReference resolved = context.getLowerBound(p.getParameterType());
					parameterTypes.set(i, resolved);
				}
			}
		}
		return closures.createFunctionTypeRef(object, parameterTypes, returnType);
	}

	protected JvmTypeReference _type(XCastedExpression object, boolean rawType) {
		return object.getType();
	}

	protected JvmTypeReference _type(XForLoopExpression object, boolean rawType) {
		return getPrimitiveVoid(object);
	}

	protected JvmTypeReference _type(XAbstractWhileExpression object, boolean rawType) {
		return getPrimitiveVoid(object);
	}

	protected JvmTypeReference _type(XTypeLiteral object, boolean rawType) {
		JvmParameterizedTypeReference typeRef = factory.createJvmParameterizedTypeReference();
		typeRef.setType(object.getType());
		return getTypeReferences().getTypeForName(Class.class, object, typeRef);
	}

	protected JvmTypeReference _type(XInstanceOfExpression object, boolean rawType) {
		return getTypeReferences().getTypeForName(Boolean.TYPE, object);
	}

	protected JvmTypeReference _type(XThrowExpression object, boolean rawType) {
		final JvmTypeReference typeForName = getPrimitiveVoid(object);
		return typeForName;
	}

	protected JvmTypeReference _type(XReturnExpression object, boolean rawType) {
		final JvmTypeReference typeForName = getPrimitiveVoid(object);
		return typeForName;
	}
	
	protected JvmTypeReference getPrimitiveVoid(XExpression object) {
		return getTypeReferences().getTypeForName(Void.TYPE, object);
	}

	protected JvmTypeReference _type(XTryCatchFinallyExpression object, boolean rawType) {
		List<JvmTypeReference> returnTypes = newArrayList();
		final JvmTypeReference getType = getType(object.getExpression(), rawType);
		returnTypes.add(getType);
		for (XCatchClause catchClause : object.getCatchClauses()) {
			JvmTypeReference type = getType(catchClause.getExpression(), rawType);
			returnTypes.add(type);
		}
		JvmTypeReference commonSuperType = getCommonType(returnTypes);
		return commonSuperType;
	}

	protected JvmTypeReference _type(XAbstractFeatureCall featureCall, boolean rawType) {
		JvmIdentifiableElement feature = featureCall.getFeature();
		if (feature == null || feature.eIsProxy())
			return null;
		JvmTypeReference featureType = getTypeForIdentifiable(feature, rawType);
		JvmTypeParameterDeclarator nearestTypeParameterDeclarator = getNearestTypeParameterDeclarator(featureCall);
		if (isResolved(featureType, nearestTypeParameterDeclarator, rawType)) {
			return featureType;
		} else {
			// method was either already called with rawType==false or the featureType points directly to a JvmTypeParameter
			rawType = false;
		}
		JvmTypeReference receiverType = getReceiverType(featureCall, rawType);
//		JvmTypeReference receiverType = convertIfNeccessary(featureCall, receiverType2, feature, rawType);
		if (feature instanceof JvmOperation) {
			JvmOperation operation = (JvmOperation) feature;
			if (featureCall.getTypeArguments().isEmpty()) {
				TypeArgumentContext context = getTypeArgumentContextProvider().getNullContext();
				JvmTypeReference result = featureType;
				if (receiverType != null) {
					context = getTypeArgumentContextProvider().injectReceiverContext(context, receiverType);
					result = context.getUpperBound(featureType, featureCall);
					if (isResolved(result, nearestTypeParameterDeclarator, rawType)) {
						return result;
					}
				}
				JvmTypeReference[] argumentTypes = getArgumentTypes(featureCall, rawType);
				if (argumentTypes.length != 0 || operation.isVarArgs()) {
					context = getTypeArgumentContextProvider().injectArgumentTypeContext(context, operation, true, argumentTypes);
					result = context.getUpperBound(featureType, featureCall);
					if (isResolved(result, nearestTypeParameterDeclarator, rawType)) {
						return result;
					}
				}
				JvmTypeReference expectedType = getExpectedType(featureCall, rawType);
				if (expectedType != null) {
					context = getTypeArgumentContextProvider().injectExpectedTypeContext(context, operation, expectedType);
					result = context.getUpperBound(featureType, featureCall);
					if (isResolved(result, nearestTypeParameterDeclarator, rawType)) {
						return result;
					}
				}
				// try again to resolve the type parameters, this time with empty var args
				if (operation.isVarArgs() && operation.getParameters().size() > argumentTypes.length) {
					context = getTypeArgumentContextProvider().injectArgumentTypeContext(context, operation, false, argumentTypes);
					result = context.getUpperBound(featureType, featureCall);
					if (isResolved(result, nearestTypeParameterDeclarator, rawType)) {
						return result;
					}
				}
				result = context.getUpperBound(featureType, operation);
//				if (!isResolved(result, nearestTypeParameterDeclarator, rawType)) {
//					// TODO temporary HACK
//					if (result instanceof JvmParameterizedTypeReference) {
//						JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) result;
//						if (parameterized.getArguments().isEmpty()) {
//							if (parameterized.getType() instanceof JvmTypeParameterDeclarator) {
//								JvmTypeParameterDeclarator declarator = (JvmTypeParameterDeclarator) parameterized.getType();
//								if (!declarator.getTypeParameters().isEmpty()) {
//									result = featureType;
//								}
//							}
//						}
//					}
//				}
				return result;
			} else {
				TypeArgumentContext context = getTypeArgumentContextProvider().getExplicitMethodInvocationContext(operation,
						receiverType, featureCall.getTypeArguments());
				JvmTypeReference result = context.getUpperBound(featureType, featureCall);
				return result;
			}
		} else {
			JvmTypeReference expectedType = rawType ? null : getExpectedType(featureCall, rawType);
			TypeArgumentContext context = getTypeArgumentContextProvider()
					.getReceiverContext(receiverType, featureType, expectedType);
			JvmTypeReference result = context.getUpperBound(featureType, featureCall);
			return result;
		}
	}

	@Inject
	private SynonymTypesProvider synonymTypesProvider;

//	protected JvmTypeReference convertIfNeccessary(
//			XAbstractFeatureCall context, 
//			JvmTypeReference toBeConverted,
//			JvmIdentifiableElement feature, 
//			boolean rawType) {
//		if (toBeConverted != null && feature instanceof JvmMember) {
//			JvmDeclaredType declaringType = ((JvmMember) feature).getDeclaringType();
//			if (synonymTypesProvider.hasSynonymTypes(toBeConverted)) {
//				final JvmTypeReference findCompatibleSynonymType = synonymTypesProvider.findCompatibleSynonymType(
//						toBeConverted, declaringType);
//				return findCompatibleSynonymType != null ? findCompatibleSynonymType : toBeConverted;
//			}
//		}
//		return toBeConverted;
//	}

	protected JvmTypeReference _typeForIdentifiable(XSwitchExpression object, boolean rawType) {
		if (object.getLocalVarName() != null) {
			final JvmTypeReference result = getType(object.getSwitch(), rawType);
			return result;
		}
		return null;
	}

	protected JvmTypeReference _typeForIdentifiable(XCasePart object, boolean rawType) {
		if (object.getTypeGuard() != null) {
			return object.getTypeGuard();
		}
		return null;
	}

	protected JvmTypeReference _typeForIdentifiable(XVariableDeclaration object, boolean rawType) {
		if (object.getType() != null)
			return object.getType();
		return getType(object.getRight(), rawType);
	}

	protected JvmTypeReference _typeForIdentifiable(JvmFormalParameter parameter, boolean rawType) {
		if (parameter.getParameterType() == null) {
			if (parameter.eContainer() instanceof XClosure) {
				final XClosure closure = (XClosure) parameter.eContainer();
				JvmTypeReference type = getExpectedType(closure, rawType);
				if (type == null) {
					return getTypeReferences().getTypeForName(Object.class, parameter);
				}
				int indexOf = closure.getFormalParameters().indexOf(parameter);
				JvmOperation operation = closures.findImplementingOperation(type, parameter.eResource());
				if (operation != null && indexOf < operation.getParameters().size()) {
					JvmFormalParameter declaredParam = getParam(operation, indexOf);
					if (rawType) {
						if (declaredParam.getParameterType().getType() instanceof JvmTypeParameter) {
							JvmTypeReference result = _typeForIdentifiable(parameter, false);
							return result;
						}
					}
					TypeArgumentContext context = getTypeArgumentContextProvider().getReceiverContext(type);
					JvmTypeReference result = context.getLowerBound(declaredParam.getParameterType());
					if (result != null) {
						return result;
					}
					result = context.resolve(declaredParam.getParameterType());
					return result;
				}
				return null;
			} else if (parameter.eContainer() instanceof XForLoopExpression) {
				XForLoopExpression forLoop = (XForLoopExpression) parameter.eContainer();
				JvmTypeReference reference = getType(forLoop.getForExpression(), false);
				if (reference == null)
					return null;
				TypeArgumentContext context = getTypeArgumentContextProvider().getReceiverContext(reference);
				final String iterableName = Iterable.class.getName();
				// TODO remove the special array treatment and put into some generic facility
				if (reference instanceof JvmGenericArrayTypeReference) {
					JvmTypeReference type = ((JvmGenericArrayTypeReference) reference).getComponentType();
					return type;
				}
				if (reference.getType() instanceof JvmArrayType) {
					JvmArrayType type = (JvmArrayType) reference.getType();
					JvmTypeReference result = getTypeReferences().createTypeRef(type);
					return result;
				}
				if (!reference.getType().getIdentifier().equals(iterableName)) {
					try {
						final Set<JvmTypeReference> collectSuperTypes = collector.collectSuperTypes(reference);
						reference = find(collectSuperTypes, new Predicate<JvmTypeReference>() {
							public boolean apply(JvmTypeReference input) {
								return input.getType().getIdentifier().equals(iterableName);
							}
						});
					} catch (NoSuchElementException e) {
						return null;
					}
				}
				if (reference instanceof JvmParameterizedTypeReference) {
					JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) reference;
					if (parameterized.getArguments().size() > 0) {
						JvmTypeReference result = context.getUpperBound(parameterized.getArguments().get(0), parameter);
						return result;
					}
				}
			}
		}
		return parameter.getParameterType();
	}
	
	@Override
	protected JvmTypeReference handleCycleGetTypeForIdentifiable(JvmIdentifiableElement identifiableElement,
			boolean rawType) {
		if (identifiableElement instanceof JvmFormalParameter && identifiableElement.eContainer() instanceof XClosure) {
			return _typeForIdentifiable((JvmFormalParameter) identifiableElement, rawType);
		}
		return super.handleCycleGetTypeForIdentifiable(identifiableElement, rawType);
	}

	protected JvmTypeReference _typeForIdentifiable(JvmGenericType thisOrSuper, boolean rawType) {
		JvmParameterizedTypeReference reference = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference();
		reference.setType(thisOrSuper);
		for (JvmTypeParameter param : thisOrSuper.getTypeParameters()) {
			JvmParameterizedTypeReference paramReference = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference();
			paramReference.setType(param);
			reference.getArguments().add(paramReference);
		}
		return reference;
	}
	
	protected JvmTypeReference _typeForIdentifiable(JvmConstructor constructor, boolean rawType) {
		JvmParameterizedTypeReference reference = factory.createJvmParameterizedTypeReference();
		JvmDeclaredType declaringType = constructor.getDeclaringType();
		reference.setType(declaringType);
		if (declaringType instanceof JvmGenericType) {
			for(JvmTypeParameter typeParam: ((JvmGenericType)declaringType).getTypeParameters()) {
				reference.getArguments().add(getTypeReferences().createTypeRef(typeParam));
			}
		}
		return reference;
	}

	protected JvmTypeReference _typeForIdentifiable(JvmField field, boolean rawType) {
		return field.getType();
	}

	protected JvmTypeReference _typeForIdentifiable(JvmOperation operation, boolean rawType) {
		return operation.getReturnType();
	}
	
	protected JvmTypeReference _typeForIdentifiable(JvmType type, boolean rawType) {
		return getTypeReferences().createTypeRef(type);
	}

	protected void _earlyExits(XClosure expr, EarlyExitAcceptor a) {
		// Don't go into closures
	}
	
	protected void _earlyExits(XReturnExpression expr, EarlyExitAcceptor acceptor) {
		if (expr.getExpression()!=null) {
			JvmTypeReference type = getType(expr.getExpression());
			if (type != null)
				acceptor.returns.add(type);
		}
	}
	
	protected void _earlyExits(XThrowExpression expr, EarlyExitAcceptor acceptor) {
		if (expr.getExpression()!=null) {
			JvmTypeReference type = getType(expr.getExpression());
			if (type != null)
				acceptor.thrown.add(type);
		}
	}
	
	protected void _earlyExits(XConstructorCall expr, EarlyExitAcceptor acceptor) {
		Iterable<JvmTypeReference> thrownExceptions = getThrownExceptionForIdentifiable(expr.getConstructor());
		if (thrownExceptions!=null) {
			acceptor.appendThrown(thrownExceptions);
		}
		_earlyExits((EObject)expr, acceptor);
	}
	
	protected void _earlyExits(XAbstractFeatureCall expr, EarlyExitAcceptor acceptor) {
		Iterable<JvmTypeReference> thrownExceptions = getThrownExceptionForIdentifiable(expr.getFeature());
		if (thrownExceptions!=null) {
			acceptor.appendThrown(thrownExceptions);
		}
		_earlyExits((EObject)expr, acceptor);
	}
	
	protected void _earlyExits(XTryCatchFinallyExpression expr, EarlyExitAcceptor acceptor) {
		EarlyExitAcceptor innerAcceptor = new EarlyExitAcceptor();
		internalCollectEarlyExits(expr.getExpression(), innerAcceptor);
		acceptor.returns.addAll(innerAcceptor.returns);
		for (XCatchClause catchClause : expr.getCatchClauses()) {
			Iterator<JvmTypeReference> iterator = innerAcceptor.thrown.iterator();
			while (iterator.hasNext()) {
				JvmTypeReference thrown = iterator.next();
				if (getTypeConformanceComputer().isConformant(catchClause.getDeclaredParam().getParameterType(), thrown)) {
					iterator.remove();
				}
			}
			internalCollectEarlyExits(catchClause.getExpression(), acceptor);
		}
		acceptor.thrown.addAll(innerAcceptor.thrown);
		if (expr.getFinallyExpression()!=null)
			internalCollectEarlyExits(expr.getFinallyExpression(), acceptor);
	}
	
	protected TypesFactory getTypesFactory() {
		return factory;
	}
	
	public Iterable<JvmTypeReference> getThrownExceptionForIdentifiable(JvmIdentifiableElement identifiable) {
		if (identifiable==null || identifiable.eIsProxy()) {
			return emptySet();
		}
		if (identifiable instanceof JvmExecutable) {
			return ((JvmExecutable) identifiable).getExceptions();
		}
		return emptySet();
	}
	
}
