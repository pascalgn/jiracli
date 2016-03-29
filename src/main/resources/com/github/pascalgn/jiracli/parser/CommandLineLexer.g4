/*
 * Copyright 2016 Pascal
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

lexer grammar CommandLineLexer;

NAME
	: [a-z][a-z0-9]*
	| '?'
	;

TEXT
	: ~[\n\r\t |a-z]+
	;

NEWLINE
	: '\n'
	| '\r\n'
	| '\r'
	;

SPACE
	: (' '|'\t')+
	;

PIPE
	: '|'
	;

QUOTED_TEXT
	: '\'' QUOTED_TEXT_CONTENT? '\''
	;

fragment
QUOTED_TEXT_CONTENT
	: (~('\'' | '\\' | '\r' | '\n') | '\\' ('\'' | '\\') | ESCAPE_SEQUENCE)+
	;

DOUBLE_QUOTED_TEXT
	: '"' DOUBLE_QUOTED_TEXT_CONTENT? '"'
	;

fragment
DOUBLE_QUOTED_TEXT_CONTENT
	: (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\') | ESCAPE_SEQUENCE)+
	;

fragment
ESCAPE_SEQUENCE
	: '\\' [btnfr]
	| UNICODE_ESCAPE
	;

fragment
UNICODE_ESCAPE
	: '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	;

fragment
HEX_DIGIT
	: [0-9A-Fa-f]
	;
