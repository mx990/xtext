package xtext.tutorial.basics

import java.util.List
import xtext.tutorial.util.Shape
import xtext.tutorial.util.Circle
import xtext.tutorial.util.Rectangle

class Xtend04_ControlStructures {
	
	/**
	 * if expressions look exactly like Java's if statements.
	 */
	ifExpression(String param) {
		if (param!=null) {
			param.length
		} else {
			0
		} 
	}
	
	/**
	 * ... but note that they are expression so they are more like Java's
	 * ternary operator.
	 */
	ifExpression_01(String param) {
		ifExpression(if (param=='foo') 'bar' else 'baz') 
	}
	
	/**
	 * ... but the else branch is optional and if not specified defaults to 'else null'
	 */
	ifExpression_02(String param) {
		ifExpression_01(if (param=='bar') 'foo') 
	}
	
	/**
	 * The switch expression is very different to the one from Java
	 * It supports dispatching over types, it has no fall through, and it uses a first match wins strategy.
	 */
	switchExpression_01(Shape shape) {
		switch (shape) {
			Circle 	: 
				'a circle : diameter='+shape.diameter
			Rectangle case shape.height == shape.width : 
				'a square : size='+shape.width
			Rectangle : 
				'a rectangle : width='+shape.width+', height='+shape.height
		}
	}
	
	/**
	 * switch can also be used more traditionally (without any type guards)
	 */
	switchExpression_02(String value) {
		switch(value) {
			case 'foo' : "it's foo"
			case 'bar' : 'a bar'
			default : "don't know"
		}	
	}
}