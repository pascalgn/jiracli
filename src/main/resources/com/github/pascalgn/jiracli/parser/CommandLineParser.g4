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

parser grammar CommandLineParser;

options { tokenVocab=CommandLineLexer; }


commandLine
	: commandPipeline (SPACE* SEMICOLON SPACE* commandPipeline)* EOF
	;

commandPipeline
	: command (SPACE* PIPE SPACE* command)*
	;

command
	: commandName (SPACE+ argument)*
	;

commandName
	: NAME
	;

argument
	: QUOTED_TEXT
	| DOUBLE_QUOTED_TEXT
	| argumentPart+
	;

argumentPart
	: NAME
	| TEXT
	;
