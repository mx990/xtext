package xtext.tutorial.basics

import xtext.tutorial.util.Person

class Xtend11_PersonExtensionsSubclass extends Xtend11_PersonExtensions {
	
	override fullName(Person p) {
		p.name+", "+p.forename 
	}
}