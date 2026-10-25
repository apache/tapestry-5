// Copyright 2008, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

lexer grammar PropertyExpressionLexer;


options
{
  superClass='org.apache.tapestry5.beanmodel.internal.antlr.BaseLexer';
}

@header
{
package org.apache.tapestry5.beanmodel.internal.antlr;
}

	
// Integer constant
fragment INTEGER
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};	
	
// Read a property or invoke a method.
fragment DEREF 
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};	
	
// Range operator, ".." between two integers.	
fragment RANGEOP
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};	
	
// Decimal number	
fragment DECIMAL
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};	
			
fragment LETTER 
	:	('a'..'z'|'A'..'Z');
fragment DIGIT 
	:	'0'..'9';	
fragment SIGN
	:	('+'|'-');
LPAREN 	:	'(';
RPAREN 	:	')';
LBRACKET:	'[';
RBRACKET:	']';
COMMA	:	',';
BANG	:	'!';
LBRACE	:	'{';
RBRACE	:	'}';
COLON	:	':';

fragment QUOTE
	:	'\'';

// Identifiers and the case-insensitive keywords are scanned by BaseLexer.nextToken(),
// because their characters are defined by Character.isJavaIdentifierStart/Part.
// These rules only provide the token types.

fragment NULL
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};
fragment TRUE
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};
fragment FALSE
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};
fragment THIS
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};
fragment IDENTIFIER
	:	{this.getClass(); /* Fix java.lang.VerifyError: Stack size too large */};

// The Safe Dereference operator understands not to de-reference through
// a null.

SAFEDEREF 
	: 	'?.';

WS 	:	(' '|'\t'|'\n'|'\r')+ { skip(); };


// Literal strings are always inside single quotes.
STRING
	:	QUOTE (options {greedy=false;} : .)* QUOTE { setText(getText().substring(1, getText().length()-1)); };


// Special rule that uses parsing tricks to identify numbers and ranges; it's all about
// the dot ('.').
// Recognizes:
// '.' as DEREF
// '..' as RANGEOP
// INTEGER (sign? digit+)
// DECIMAL (sign? digits* . digits+)
// Has to watch out for embedded rangeop (i.e. "1..10" is not "1." and ".10").

NUMBER_OR_RANGEOP
	:	SIGN? DIGIT+
		(
			{ input.LA(2) != '.' }? => '.' DIGIT* {   $type = DECIMAL; stripLeadingPlus(); }
			| {  $type = INTEGER;  stripLeadingPlus(); }
		)
		
	|	SIGN '.' DIGIT+ {  $type = DECIMAL;  stripLeadingPlus(); }
	
	|	'.'
		( 
			DIGIT+ { $type = DECIMAL; stripLeadingPlus();}
			| '.' {$type = RANGEOP; }
			| {$type = DEREF; }
		)
	;	

