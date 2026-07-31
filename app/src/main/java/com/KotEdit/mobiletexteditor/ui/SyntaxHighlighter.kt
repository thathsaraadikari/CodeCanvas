package com.KotEdit.mobiletexteditor.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class SyntaxTransformation(
    private val fileName: String,
    private val searchQuery: String = ""
) : VisualTransformation {

    // Define colors for our syntax
    private val keywordColor = Color(0xFFCC7832)
    private val stringColor = Color(0xFF6A8759)
    private val commentColor = Color(0xFF808080)
    private val mdHeaderColor = Color(0xFF2B82C9)
    private val mdCodeColor = Color(0xFFA9B7C6)
    private val searchHighlightColor = Color(0xFFFFFF00) // Yellow

    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = buildAnnotatedString {
            append(text.text) // Start with the raw text

            if (fileName.endsWith(".kt")) {
                applyKotlinSyntax(this, text.text)
            } else if (fileName.endsWith(".md")) {
                applyMarkdownSyntax(this, text.text)
            }
            
            // Apply search highlighting on top of everything
            if (searchQuery.isNotEmpty()) {
                var index = text.text.indexOf(searchQuery, ignoreCase = true)
                while (index >= 0) {
                    addStyle(
                        style = SpanStyle(background = searchHighlightColor, color = Color.Black),
                        start = index,
                        end = index + searchQuery.length
                    )
                    index = text.text.indexOf(searchQuery, index + searchQuery.length, ignoreCase = true)
                }
            }
        }
        return TransformedText(highlighted, OffsetMapping.Identity)
    }

    private fun applyKotlinSyntax(builder: AnnotatedString.Builder, text: String) {
        // 1. Highlight Keywords
        val keywordRegex = "\\b(fun|val|var|class|interface|return|if|else|when|for|while|import|package|private|public|protected|internal|override|true|false|null)\\b".toRegex()
        keywordRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }

        // 2. Highlight Strings ("text")
        val stringRegex = "\".*?\"".toRegex()
        stringRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
        }

        // 3. Highlight Single-line Comments (// text)
        val commentRegex = "//.*".toRegex()
        commentRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = commentColor, fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
        }
    }

    private fun applyMarkdownSyntax(builder: AnnotatedString.Builder, text: String) {
        // 1. Headers (# Header)
        val headerRegex = "^#{1,6}\\s.*$".toRegex(RegexOption.MULTILINE)
        headerRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = mdHeaderColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }

        // 2. Bold (**text**)
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
        boldRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }

        // 3. Italic (*text*)
        val italicRegex = "(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)".toRegex()
        italicRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
        }

        // 4. Inline Code (`code`)
        val codeRegex = "`([^`]*)`".toRegex()
        codeRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = mdCodeColor, background = Color(0xFF2B2B2B)), match.range.first, match.range.last + 1)
        }
    }
}
