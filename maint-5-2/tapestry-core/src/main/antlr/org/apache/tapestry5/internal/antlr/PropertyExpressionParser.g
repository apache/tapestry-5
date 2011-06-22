// Copyright 2008, 2009 The Apache Software Foundation
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

parser grammar PropertyExpressionParser;


options
{
  superClass='org.apache.tapestry5.internal.antlr.BaseParser';
  output=AST;		
  ASTLabelType=CommonTree;
  tokenVocab=PropertyExpressionLexer;
  backtrack=true;
}

tokens
{	
	// Parser token representing a method invocation
    	INVOKE;
    	// A List (top level, or as method parameter)
    	LIST;
    	// Not operation (invert a boolean)
    	NOT;
}

@header
{
package org.apache.tapestry5.internal.antlr;
}

	
start 	:	expression^ EOF!;
		
expression
	:	keyword
	|	rangeOp
	|	constant
	|	propertyChain
	|	list
	|	notOp
	;
	
keyword	:	NULL | TRUE | FALSE | THIS;

constant:	INTEGER| DECIMAL | STRING;	
	
propertyChain
	:	term DEREF propertyChain -> ^(DEREF term propertyChain)
	|	term SAFEDEREF propertyChain -> ^(SAFEDEREF term propertyChain)
	|	term
	;	
	
term	:	IDENTIFIER
	|	methodInvocation
	;
	
methodInvocation
	:	id=IDENTIFIER LPAREN RPAREN -> ^(INVOKE $id)
	|	id=IDENTIFIER LPAREN expressionList RPAREN -> ^(INVOKE $id expressionList)
	;	
	
expressionList
	:	expression (COMMA! expression)*
	;	
	
rangeOp
	:	from=rangeopArg  RANGEOP to=rangeopArg -> ^(RANGEOP $from $to)
	;	
	
rangeopArg 
	:	INTEGER
	|	propertyChain
	;	
	
list	:	LBRACKET RBRACKET -> ^(LIST)
	|	LBRACKET expressionList RBRACKET -> ^(LIST expressionList)
	;	
	
	
notOp 	:	BANG expression -> ^(NOT expression)
	;
