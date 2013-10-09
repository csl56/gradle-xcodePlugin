package org.openbakery.output

import org.gradle.logging.StyledTextOutput

import java.util.regex.Matcher

/**
 * Created with IntelliJ IDEA.
 * User: rene
 * Date: 02.10.13
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class XcodeBuildOutputAppender implements OutputAppender {


	boolean command = false
	String currentSourceFile
	StringBuilder outputText = new StringBuilder()
	boolean hasOutput = false
	boolean warning = false
	boolean error = false

	StyledTextOutput output

	XcodeBuildOutputAppender(StyledTextOutput output) {
		this.output = output
		reset()
	}

	void reset() {
		currentSourceFile = null
		outputText = new StringBuilder()
		hasOutput = false
		error = false
		warning = false
	}

	@Override
	void append(String line) {

		if (hasOutput) {
			outputText.append("\n")
			outputText.append(line)
		}

		if (line.startsWith("CompileC")) {
			int sourceFileStartIndex = line.indexOf(".o")+3;
			int sourceFileEndIndex = line.indexOf(".m")+2;
			if (sourceFileEndIndex < line.length()) {
				currentSourceFile = line.substring(sourceFileStartIndex, sourceFileEndIndex)
				command = true
			}
		} else if (line.startsWith("CompileStoryboard") || line.startsWith("CompileXIB")) {
			int sourceFileStartIndex = line.indexOf(" ")+1;
			if (sourceFileStartIndex < line.length()) {
				currentSourceFile = line.substring(sourceFileStartIndex, line.length())
				command = true
			}
		} else if (currentSourceFile != null && line.equals("")) {
			// end of command
			if (error) {
				output.withStyle(StyledTextOutput.Style.Failure).text("   ERROR")
			} else if (warning) {
				output.withStyle(StyledTextOutput.Style.Identifier).text("WARNINGS")
			} else {
				output.withStyle(StyledTextOutput.Style.Identifier).text("      OK")
			}
			output.text(" - Compile: " + currentSourceFile);
			output.println();
			if (hasOutput) {
				output.println(outputText.toString())
			}

			reset()
		} else if (line.endsWith("errors generated.") || line.endsWith("error generated.")) {
			error = true
		} else if (line.endsWith("warnings generated.") || line.endsWith("warning generated.")) {
			warning = true
		} else if (!hasOutput && currentSourceFile != null && line.contains(currentSourceFile) && !line.startsWith(" ")) {
			hasOutput = true
		}

	}
}
